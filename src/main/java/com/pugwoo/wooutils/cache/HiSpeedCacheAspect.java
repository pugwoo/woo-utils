package com.pugwoo.wooutils.cache;

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

    private static Map<String, Object> dataMap = new ConcurrentHashMap<>(); // 存缓存数据的
    private static Map<Long, List<String>> expireLineMap = new TreeMap<>(); // 存数据超时时间的，超时时间 -> 对应于该超时时间的key的列表
    private static Map<String, Long> keyExpireMap = new ConcurrentHashMap<>(); // 每个key的超时时间，key -> 超时时间

    private static Map<String, ProceedingJoinPoint> serviceMap = new ConcurrentHashMap<>();

    private static Map<String, List<Long>> intervalMap = new ConcurrentHashMap<>(); //缓存更新时间的map

    private static volatile CleanExpireDataTask cleanThread = null;
    private static volatile ContinueUpdateTask continueThread = null;

    @Around("@annotation(com.pugwoo.wooutils.cache.HiSpeedCache) execution(* *.*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method targetMethod = signature.getMethod();
        String clazzName = signature.getDeclaringType().getName();
        String methodName = targetMethod.getName();

        HiSpeedCache hiSpeedCache = targetMethod.getAnnotation(HiSpeedCache.class);
        String key = "";
        String keyScript = hiSpeedCache.keyScript();
        if (StringTools.isNotBlank(keyScript)) {
            Object[] args = pjp.getArgs();
            Map<String, Object> context = MapUtils.of("args", args);
            try {
                Object result = MVEL.eval(keyScript.trim(), context);
                if (result != null) {
                    key = result.toString();
                }
            } catch (Throwable e) {
                LOGGER.error("eval keyScript fail, keyScript:{}, args:{}", keyScript, JSON.toJson(args));
            }
        }

        String cacheKey = clazzName + "." + methodName + ":" + targetMethod.hashCode() + (key.isEmpty() ? "" : ":" + key);

        int fetchSecond = hiSpeedCache.continueFetchSecond();
        long expireSecond = hiSpeedCache.expireSecond();
        if (fetchSecond != 0) {
            expireSecond = fetchSecond;
            initIntervalTime(cacheKey, hiSpeedCache.expireSecond(), fetchSecond);
        }
        long expireTime = expireSecond * 1000 + System.currentTimeMillis();

        if (!serviceMap.containsKey(cacheKey)) {
            serviceMap.put(cacheKey, pjp);
        }

        setExpireTime(expireTime, cacheKey);

        if (dataMap.containsKey(cacheKey)) {
            return dataMap.get(cacheKey);
        }

        Object ret = pjp.proceed();
        dataMap.put(cacheKey, ret);

        if (cleanThread == null) {
            synchronized (CleanExpireDataTask.class) {
                if (cleanThread == null) {
                    cleanThread = new CleanExpireDataTask();
                    cleanThread.start();
                }
            }
        }

        if (fetchSecond != 0) {
            if (continueThread == null) {
                synchronized (CleanExpireDataTask.class) {
                    if (continueThread == null) {
                        continueThread = new ContinueUpdateTask();
                        continueThread.start();
                    }
                }
            }
        }

        return ret;
    }

    private synchronized static void initIntervalTime(String cacheKey, int expireTime, int continueTime) {

        int length = (int) Math.ceil(continueTime / (expireTime * 1.0));
        long startTime = System.currentTimeMillis();
        List<Long> intervals = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            intervals.add(startTime + expireTime * i * 1000);
        }
        intervalMap.put(cacheKey, intervals);
    }

    private synchronized static void setExpireTime(long expireTime, String cacheKey) {
        // 清理 之前保存下来的 expireLineMap
        if (keyExpireMap.containsKey(cacheKey)) {
            Long defaultExpire = keyExpireMap.get(cacheKey);
            if (expireLineMap.containsKey(defaultExpire)) {
                List<String> expireList = expireLineMap.get(defaultExpire);
                if (expireList.size() == 1) {
                    expireLineMap.remove(defaultExpire);
                } else {
                    expireList.removeIf(key -> key.equals(cacheKey));
                }
                keyExpireMap.remove(cacheKey);
            }
        }

        // 设置 超时时间
        if (expireLineMap.containsKey(expireTime)) {
            List<String> expireList = expireLineMap.get(expireTime);
            expireList.add(cacheKey);
        } else {
            ArrayList<String> lists = new ArrayList<>();
            lists.add(cacheKey);
            expireLineMap.put(expireTime, lists);
        }
        // 增加映射关系
        keyExpireMap.put(cacheKey, expireTime);

    }

    /**
     * 从expireLineMap中，按超时顺序遍历，如果超时时间小于当前时间，则清理该key对应的List<String>列表的key dataMap
     * 所以这里操作了dataMap和expireLineMap两张表
     * 对于超时时间大于当前时间的，不处理
     */
    private synchronized static void cleanExpireData() {
        List<Long> removeList = new ArrayList<>();
        for (Map.Entry<Long, List<String>> entry : expireLineMap.entrySet()) {
            Long key = entry.getKey();
            if (key < System.currentTimeMillis()) {
                entry.getValue().forEach(cacheKey -> {
                    dataMap.remove(cacheKey);
                    serviceMap.remove(cacheKey);
                    keyExpireMap.remove(cacheKey);
                    intervalMap.remove(cacheKey);
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

    private synchronized static void refreshResult() {
        for (Map.Entry<String, ProceedingJoinPoint> entry : serviceMap.entrySet()) {
            String cacheKey = entry.getKey();
            if (intervalMap.containsKey(cacheKey)) {
                List<Long> longs = intervalMap.get(cacheKey);
                List<Long> removeList = new ArrayList<>();
                for (Long limit : longs) {
                    if (limit < System.currentTimeMillis()) {
                        ProceedingJoinPoint pjps = entry.getValue();
                        Object result = null;
                        removeList.add(limit);
                        try {
                            result = pjps.proceed();
                            dataMap.put(cacheKey, result);
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    } else {
                        break;
                    }
                }

                removeList.forEach(item -> {
                    longs.remove(item);
                });
            }
        }
    }

    private static class CleanExpireDataTask extends Thread {

        @Override
        public void run() {
            while (true) {
                cleanExpireData();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }

        }
    }

    private static class ContinueUpdateTask extends Thread {

        @Override
        public void run() {
            while (true) {
                refreshResult();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }
        }
    }


}
