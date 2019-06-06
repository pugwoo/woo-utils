package com.pugwoo.wooutils.cache;

import com.pugwoo.wooutils.collect.ListUtils;
import com.pugwoo.wooutils.collect.MapUtils;
import com.pugwoo.wooutils.json.JSON;
import com.pugwoo.wooutils.string.StringTools;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.lang.reflect.Method;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EnableAspectJAutoProxy
@Aspect
public class HiSpeedCacheAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(HiSpeedCacheAspect.class);

    private static class ContinueFetchDTO {
        public volatile ProceedingJoinPoint pjp;
        public volatile int intervalSecond; // 调用的间隔
        public volatile long expireTimestamp; // 此次调用的过时时间（毫秒时间戳）
        public ContinueFetchDTO(ProceedingJoinPoint pjp, int intervalSecond, long expireTimestamp) {
            this.pjp = pjp;
            this.intervalSecond = intervalSecond;
            this.expireTimestamp = expireTimestamp;
        }
    }

    private static Map<String, Object> dataMap = new ConcurrentHashMap<>(); // 存缓存数据的
    private static Map<Long, List<String>> expireLineMap = new TreeMap<>(); // 存数据超时时间的，超时时间 -> 对应于该超时时间的key的列表
    private static Map<String, Long> keyExpireMap = new ConcurrentHashMap<>(); // 每个key的超时时间，key -> 超时时间

    private static Map<String, ContinueFetchDTO> keyContinueFetchMap = new ConcurrentHashMap<>(); // 每个key持续更新的信息
    private static Map<Long, List<String>> fetchLineMap = new TreeMap<>(); // 持续获取的时间线，里面只有每个key的最近一次获取时间

    private static volatile CleanExpireDataTask cleanThread = null;
    private static volatile ContinueUpdateTask continueThread = null; // 暂时用单线程足够了，由应用保证每个方法不应该永久卡死

    @Around("@annotation(com.pugwoo.wooutils.cache.HiSpeedCache) execution(* *.*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method targetMethod = signature.getMethod();
        String clazzName = signature.getDeclaringType().getName();
        String methodName = targetMethod.getName();

        HiSpeedCache hiSpeedCache = targetMethod.getAnnotation(HiSpeedCache.class);
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
        String cacheKey = clazzName + "." + methodName + ":" + toString(parameterTypes) + (key.isEmpty() ? "" : ":" + key);

        int fetchSecond = hiSpeedCache.continueFetchSecond();
        if(fetchSecond > 0) { // 持续更新时，每次接口的访问都会延长持续获取的时长(如果还没超时的话)
            ContinueFetchDTO continueFetchDTO = keyContinueFetchMap.get(cacheKey);
            if(continueFetchDTO != null) {
                continueFetchDTO.pjp = pjp;
                continueFetchDTO.expireTimestamp = fetchSecond * 1000 + System.currentTimeMillis();
            }
        }

        if (dataMap.containsKey(cacheKey)) {
            return dataMap.get(cacheKey);
        }

        // 当缓存中没有时进行
        Object ret = pjp.proceed();

        synchronized (HiSpeedCacheAspect.class) {
            dataMap.put(cacheKey, ret);

            long expireTime = Math.max(hiSpeedCache.expireSecond(), hiSpeedCache.continueFetchSecond())
                    * 1000 + System.currentTimeMillis();
            if (fetchSecond > 0) {
                ContinueFetchDTO continueFetchDTO = new ContinueFetchDTO(pjp, hiSpeedCache.expireSecond(),
                        expireTime);
                keyContinueFetchMap.put(cacheKey, continueFetchDTO);
                long nextFetchTime = Math.min(hiSpeedCache.expireSecond(), hiSpeedCache.continueFetchSecond())
                        * 1000 + System.currentTimeMillis();
                addFetchToTimeLine(nextFetchTime, cacheKey);
            }
            changeKeyExpireTime(cacheKey, expireTime);
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

        return ret;
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
    private static void cleanExpireData() {
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

            removeList.forEach(item -> {
                expireLineMap.remove(item);
            });
        }
    }

    /**
     * 持续调用刷新数据
     */
    private static void refreshResult() {
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
                        long nextTime = continueFetchDTO.intervalSecond * 1000 + System.currentTimeMillis();

                        try {
                            Object result = continueFetchDTO.pjp.proceed();
                            dataMap.put(cacheKey, result);
                            changeKeyExpireTime(cacheKey, Math.max(continueFetchDTO.expireTimestamp, nextTime));
                        } catch (Throwable e) {
                            LOGGER.error("refreshResult execute pjp fail, key:{}", cacheKey, e);
                        }

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

            removeList.forEach(item -> {
                fetchLineMap.remove(item);
            });

            for(int i = 0; i < addFetchToTimeLine_time.size(); i++) {
                addFetchToTimeLine(addFetchToTimeLine_time.get(i), addFetchToTimeLine_cacheKey.get(i));
            }
        }
    }

    private static class CleanExpireDataTask extends Thread {
        @Override
        public void run() {
            while (true) {
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

    private static class ContinueUpdateTask extends Thread {
        @Override
        public void run() {
            while (true) {
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
