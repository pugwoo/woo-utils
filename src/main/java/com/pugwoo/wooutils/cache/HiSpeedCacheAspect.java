package com.pugwoo.wooutils.cache;

import com.pugwoo.wooutils.collect.ListUtils;
import com.pugwoo.wooutils.collect.MapUtils;
import com.pugwoo.wooutils.json.JSON;
import com.pugwoo.wooutils.redis.IRedisObjectConverter;
import com.pugwoo.wooutils.redis.RedisHelper;
import com.pugwoo.wooutils.string.StringTools;
import com.pugwoo.wooutils.task.ExecuteThem;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@EnableAspectJAutoProxy
@Aspect
public class HiSpeedCacheAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(HiSpeedCacheAspect.class);

    /**
     * 因为ConcurrentHashMap不能存放null值，所以用这个特殊的String来代表null值，redis同理。<br>
     * 缓存null值是避免缓存穿透
     **/
    private static final String NULL_VALUE = "(NULL)HiSpeedCache@DpK3GovAptNICKAndKarenXSysudYrY";

    @Autowired(required = false)
    private RedisHelper redisHelper;

    // 多线程执行更新数据任务，默认十个线程
    private static ExecuteThem executeThem;

    public HiSpeedCacheAspect() {
        executeThem = new ExecuteThem(10);
    }

    /**
     *
     * @param nUpdateThreads 更新数据任务的线程数
     */
    public HiSpeedCacheAspect(int nUpdateThreads) {
        executeThem = new ExecuteThem(nUpdateThreads);
    }

    private static class ContinueFetchDTO {
        private volatile ProceedingJoinPoint pjp;
        private volatile HiSpeedCache hiSpeedCache;
        private volatile long expireTimestamp; // 此次调用的过时时间（毫秒时间戳）

        private ContinueFetchDTO(ProceedingJoinPoint pjp, HiSpeedCache hiSpeedCache, long expireTimestamp) {
            this.pjp = pjp;
            this.hiSpeedCache = hiSpeedCache;
            this.expireTimestamp = expireTimestamp;
        }
    }

    // 特别注意，因为expireLineMap和fetchLineMap不是线程安全，下面实现对其进行了synchronized，已经确认之间没有循环加锁，避免掉死锁的可能

    private static final Map<String, Object> dataMap = new ConcurrentHashMap<>(); // 存缓存数据的
    private static final Map<Long, List<String>> expireLineMap = new TreeMap<>(); // 存数据超时时间的，超时时间 -> 对应于该超时时间的key的列表
    private static final Map<String, Long> keyExpireMap = new ConcurrentHashMap<>(); // 每个key的超时时间，key -> 超时时间

    private static final Map<String, ContinueFetchDTO> keyContinueFetchMap = new ConcurrentHashMap<>(); // 每个key持续更新的信息
    private static final Map<Long, List<String>> fetchLineMap = new TreeMap<>(); // 持续获取的时间线，里面只有每个key的最近一次获取时间

    private static volatile CleanExpireDataTask cleanThread = null; // 不需要多线程
    private static volatile ContinueUpdateTask continueThread = null; // 不需要多线程

    @Around("@annotation(com.pugwoo.wooutils.cache.HiSpeedCache) execution(* *.*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method targetMethod = signature.getMethod();
        String clazzName = signature.getDeclaringType().getName();
        String methodName = targetMethod.getName();

        HiSpeedCache hiSpeedCache = targetMethod.getAnnotation(HiSpeedCache.class);
        boolean useRedis = false;
        if(hiSpeedCache.useRedis()) {
            if(redisHelper != null) {
                useRedis = true;
            } else {
                LOGGER.error("HiSpeedCache config useRedis=true, while there is no redisHelper");
            }
        }

        String key = "";
        String keyScript = hiSpeedCache.keyScript().trim();
        if (StringTools.isNotEmpty(keyScript)) {
            Object[] args = pjp.getArgs();
            try {
                Object result = MVEL.eval(keyScript, MapUtils.of("args", args));
                if (result != null) { // 返回结果为null等价于keyScript为空字符串
                    key = result.toString();
                }
            } catch (Throwable e) {
                LOGGER.error("eval keyScript fail, keyScript:{}, args:{}", keyScript, JSON.toJson(args));
                return pjp.proceed(); // 出现异常则等价于不使用缓存，直接调方法
            }
        }

        Class<?>[] parameterTypes = targetMethod.getParameterTypes();
        String cacheKey = "HSC:" + clazzName + "." + methodName + ":" + toString(parameterTypes) + (key.isEmpty() ? "" : ":" + key);

        int fetchSecond = hiSpeedCache.continueFetchSecond();
        if(fetchSecond > 0) { // 持续更新时，每次接口的访问都会延长持续获取的时长(如果还没超时的话)
            ContinueFetchDTO continueFetchDTO = keyContinueFetchMap.get(cacheKey);
            if(continueFetchDTO != null) {
                continueFetchDTO.pjp = pjp;
                continueFetchDTO.expireTimestamp = fetchSecond * 1000 + System.currentTimeMillis();
            }
        }

        // 查看数据是否有命中，有则直接返回
        if(useRedis) {
            String value = redisHelper.getString(cacheKey);
            if(value != null) { // == null则缓存没命中，应该走下面调用逻辑
                if(value.equals(NULL_VALUE)) {
                    return null; // 命中null值缓存
                }
                Class<?> returnClazz = targetMethod.getReturnType();
                Class<?> genericClass1 = hiSpeedCache.genericClass1();
                Class<?> genericClass2 = hiSpeedCache.genericClass2();

                IRedisObjectConverter redisObjectConverter = redisHelper.getRedisObjectConverter();
                if(genericClass1 == Void.class && genericClass2 == Void.class) {
                    return redisObjectConverter.convertToObject(value, returnClazz);
                } else if (genericClass1 != Void.class && genericClass2 == Void.class) {
                    return redisObjectConverter.convertToObject(value, returnClazz, genericClass1);
                } else {
                    return redisObjectConverter.convertToObject(value, returnClazz, genericClass1, genericClass2);
                }
            }
        } else {
            if (dataMap.containsKey(cacheKey)) {
                Object data = dataMap.get(cacheKey);
                if(data == NULL_VALUE) { // 缓存null值，因为是内存，所以可以用==比较
                    return null;
                }
                return processClone(hiSpeedCache, data);
            }
        }

        // 当缓存中没有时进行
        Object ret = pjp.proceed();

        synchronized (HiSpeedCacheAspect.class) {
            int expireSecond = Math.max(hiSpeedCache.expireSecond(), hiSpeedCache.continueFetchSecond());
            long expireTime = expireSecond * 1000 + System.currentTimeMillis();

            if(useRedis) {
                if(ret != null) {
                    redisHelper.setObject(cacheKey, expireSecond, ret);
                } else {
                    redisHelper.setString(cacheKey, expireSecond, NULL_VALUE); // 缓存null值
                }
            } else {
                if(ret != null) {
                    dataMap.put(cacheKey, ret);
                } else {
                    dataMap.put(cacheKey, NULL_VALUE); // 因为concurrentHashMap不能放null
                }
                changeKeyExpireTime(cacheKey, expireTime);
            }

            if (fetchSecond > 0) {
                ContinueFetchDTO continueFetchDTO = new ContinueFetchDTO(pjp, hiSpeedCache, expireTime);
                keyContinueFetchMap.put(cacheKey, continueFetchDTO);
                long nextFetchTime = Math.min(hiSpeedCache.expireSecond(), hiSpeedCache.continueFetchSecond())
                        * 1000 + System.currentTimeMillis();
                addFetchToTimeLine(nextFetchTime, cacheKey);
            }
        }

        if (cleanThread == null) {
            synchronized (CleanExpireDataTask.class) {
                if (cleanThread == null) {
                    cleanThread = new CleanExpireDataTask();
                    cleanThread.setName("HiSpeedCache-clean-thread");
                    cleanThread.start();
                }
            }
        }

        if (fetchSecond != 0) {
            if (continueThread == null) {
                synchronized (ContinueUpdateTask.class) {
                    if (continueThread == null) {
                        continueThread = new ContinueUpdateTask();
                        continueThread.setName("HiSpeedCache-update-thread");
                        continueThread.start();
                    }
                }
            }
        }

        return processClone(hiSpeedCache, ret);
    }

    /*处理结果值克隆的问题*/
    private Object processClone(HiSpeedCache hiSpeedCache, Object data) {
        if(data == null) {
            return null;
        }
        if(hiSpeedCache.cloneReturn()) {
            Class<?> clazz = data.getClass();
            // 基础类型、String不需要转，相当于性能优化
            if(clazz == String.class || clazz == Integer.class || clazz == Long.class) {
                return data;
            }
            Class<?> genericClass1 = hiSpeedCache.genericClass1();
            Class<?> genericClass2 = hiSpeedCache.genericClass2();
            if(genericClass1 == Void.class && genericClass2 == Void.class) {
                return JSON.parse(JSON.toJson(data), clazz);
            } else if (genericClass1 != Void.class && genericClass2 == Void.class) {
                return JSON.parse(JSON.toJson(data), clazz, genericClass1);
            } else {
                return JSON.parse(JSON.toJson(data), clazz, genericClass1, genericClass2);
            }
        } else {
            return data;
        }
    }

    /**设置或修改cacheKey的超时时间，保证一个cacheKey只有一个超时时间*/
    private static void changeKeyExpireTime(String cacheKey, long expireTime) {
        synchronized (expireLineMap) {
            Long oldExpireTime = keyExpireMap.get(cacheKey);
            if (oldExpireTime != null) { // 清理可能的老数据
                List<String> keys = expireLineMap.get(oldExpireTime);
                if(keys != null) {
                    int keysSize = keys.size();
                    if(keysSize == 0 || keysSize == 1) {
                        expireLineMap.remove(oldExpireTime);
                    } else {
                        keys.removeIf(o -> o == null || o.equals(cacheKey));
                    }
                }
            }

            // 设置进去新的超时时间
            keyExpireMap.put(cacheKey, expireTime);
            List<String> keys = expireLineMap.get(expireTime);
            if(keys == null) {
                keys = ListUtils.newArrayList(cacheKey);
                expireLineMap.put(expireTime, keys);
            } else {
                if(!keys.contains(cacheKey)) {
                    keys.add(cacheKey);
                }
            }
        }
    }

    /**将某个cacheKey的下一次获取加入到更新时间线中*/
    private static void addFetchToTimeLine(long nextTime, String cacheKey) {
        synchronized (fetchLineMap) {

            // 检查一下cacheKey是否已经存在于刷新线中，如果已经存在，则不再加入
            for(Map.Entry<Long, List<String>> e : fetchLineMap.entrySet()) {
                if(e.getValue() != null) {
                    for(String ck : e.getValue()) {
                        if(ck != null && ck.equals(cacheKey)) {
                            return; // 不加入
                        }
                    }
                }
            }

            List<String> keys = fetchLineMap.get(nextTime);
            if(keys == null) {
                fetchLineMap.put(nextTime, ListUtils.newArrayList(cacheKey));
            } else {
                if(!keys.contains(cacheKey)) {
                    keys.add(cacheKey);
                }
            }
        }
    }

    /**
     * 从expireLineMap中，按超时顺序遍历，如果超时时间小于当前时间，则清理该key对应的List(String)列表的key dataMap
     * 所以这里操作了dataMap和expireLineMap两个map
     * 对于超时时间大于当前时间的，不处理
     */
    private void cleanExpireData() {
        synchronized (expireLineMap) {

            List<Long> removeList = new ArrayList<>();
            for (Map.Entry<Long, List<String>> entry : expireLineMap.entrySet()) {
                Long key = entry.getKey();
                if (key <= System.currentTimeMillis()) {
                    entry.getValue().forEach(cacheKey -> {
                        dataMap.remove(cacheKey);
                        keyExpireMap.remove(cacheKey);
                    });
                    removeList.add(key);
                } else {
                    break;
                }
            }

            removeList.forEach(expireLineMap::remove);
        }
    }

    /**
     * 持续调用刷新数据
     */
    private void refreshResult() {
        synchronized (fetchLineMap) {

            List<Long> removeList = new ArrayList<>();

            List<Long> addFetchToTimeLine_time = new ArrayList<>();
            List<String> addFetchToTimeLine_cacheKey = new ArrayList<>();

            for (Map.Entry<Long, List<String>> entry : fetchLineMap.entrySet()) {
                Long key = entry.getKey();
                if (key <= System.currentTimeMillis()) {
                    entry.getValue().forEach(cacheKey -> {
                        ContinueFetchDTO continueFetchDTO = keyContinueFetchMap.get(cacheKey);
                        if(continueFetchDTO == null) {
                            return;
                        }
                        // 安排下一次调用
                        long nextTime = continueFetchDTO.hiSpeedCache.expireSecond() * 1000 + System.currentTimeMillis();

                        // 多线程执行更新任务
                        executeThem.add(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Object result = continueFetchDTO.pjp.proceed();
                                    if(continueFetchDTO.hiSpeedCache.useRedis()) {
                                        int expireSecond = Math.max(continueFetchDTO.hiSpeedCache.expireSecond(),
                                                continueFetchDTO.hiSpeedCache.continueFetchSecond());
                                        if(result != null) {
                                            redisHelper.setObject(cacheKey, expireSecond , result);
                                        } else {
                                            redisHelper.setString(cacheKey, expireSecond, NULL_VALUE);
                                        }
                                    } else {
                                        if(result != null) {
                                            dataMap.put(cacheKey, result);
                                        } else {
                                            dataMap.put(cacheKey, NULL_VALUE); // 因为concurrentHashMap不能放null
                                        }
                                        changeKeyExpireTime(cacheKey, Math.max(continueFetchDTO.expireTimestamp, nextTime));
                                    }
                                } catch (Throwable e) {
                                    LOGGER.error("refreshResult execute pjp fail, key:{}", cacheKey, e);
                                }
                            }
                        });

                        if(nextTime <= continueFetchDTO.expireTimestamp) { // 下一次调用还在超时时间内
                            addFetchToTimeLine_time.add(nextTime);
                            addFetchToTimeLine_cacheKey.add(cacheKey);
                        } else {
                            keyContinueFetchMap.remove(cacheKey); // 清理continueFetchDTO
                        }
                    });
                    removeList.add(key);
                } else {
                    break;
                }
            }

            removeList.forEach(fetchLineMap::remove);

            for(int i = 0; i < addFetchToTimeLine_time.size(); i++) {
                addFetchToTimeLine(addFetchToTimeLine_time.get(i), addFetchToTimeLine_cacheKey.get(i));
            }
        }
    }

    private class CleanExpireDataTask extends Thread {
        @Override
        public void run() {
            while (true) { // 一直循环，不会退出
                try {
                    cleanExpireData();
                } catch (Throwable e) { // 保证线程存活
                    LOGGER.error("clean expire data error", e);
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) { // ignore
                }
            }
        }
    }

    private class ContinueUpdateTask extends Thread {
        @Override
        public void run() {
            while (true) { // 一直循环，不会退出
                try {
                    refreshResult();
                } catch (Throwable e) { // 保证线程存活
                    LOGGER.error("refresh result error", e);
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) { // ignore
                }
            }
        }
    }

    /**将参数类型转换成字符串*/
    private static String toString(Class<?>[] parameterTypes) {
        if(parameterTypes == null || parameterTypes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(Class<?> clazz : parameterTypes) {
            sb.append(clazz.getName()).append(",");
        }
        return sb.toString().substring(0, sb.length() - 1);
    }

}
