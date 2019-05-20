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
	private static Map<Long, List<String>> expireLineMap = new LinkedHashMap<>(); // 存数据超时时间的，超时时间 -> 对应于该超时时间的key的列表
	// private static Map<String, Long> keyExpireMap = new ConcurrentHashMap<>(); // 每个key的超时时间，key -> 超时时间
	private static volatile CleanExpireDataTask cleanThread = null;

	@Around("@annotation(com.pugwoo.wooutils.cache.HiSpeedCache) execution(* *.*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
		MethodSignature signature = (MethodSignature) pjp.getSignature();
		Method targetMethod = signature.getMethod();
		String clazzName = signature.getDeclaringType().getName();
		String methodName = targetMethod.getName();

		HiSpeedCache hiSpeedCache = targetMethod.getAnnotation(HiSpeedCache.class);

		String key = "";
		String keyScript = hiSpeedCache.keyScript();
		if(StringTools.isNotBlank(keyScript)) {
			Object[] args = pjp.getArgs();
			Map<String, Object> context =MapUtils.of("args", args);
			try {
				Object result = MVEL.eval(keyScript.trim(), context);
				if(result != null) {
					key = result.toString();
				}
			} catch (Throwable e) {
				LOGGER.error("eval keyScript fail, keyScript:{}, args:{}", keyScript, JSON.toJson(args));
			}
		}

		String cacheKey = clazzName + "." + methodName + ":" + targetMethod.hashCode() + (key.isEmpty() ? "" : ":" + key);
		if(dataMap.containsKey(cacheKey)) {
			return dataMap.get(cacheKey);
		}
		Object ret = pjp.proceed();
		long expireTime = hiSpeedCache.expireSecond()*1000+System.currentTimeMillis();
		dataMap.put(cacheKey, ret);
		setExpireTime(expireTime,cacheKey);
		if (cleanThread == null) {
			synchronized (CleanExpireDataTask.class) {
				if (cleanThread == null) {
					cleanThread = new CleanExpireDataTask();
					cleanThread.start();
				}
			}
		}
		return  ret;
    }

    private  synchronized  static  void setExpireTime(long expireTime,String cacheKey){
		if(expireLineMap.containsKey(expireTime)){
			List<String> expireList = expireLineMap.get(expireTime);
			expireList.add(cacheKey);
		}else {
			ArrayList<String> lists = new ArrayList<>();
			lists.add(cacheKey);
			expireLineMap.put(expireTime,lists);
		}
	}

	/**
	 * 	 从expireLineMap中，按超时顺序遍历，如果超时时间小于当前时间，则清理该key对应的List<String>列表的key dataMap
	 *   所以这里操作了dataMap和expireLineMap两张表
	 *   对于超时时间大于当前时间的，不处理
	 */
	private synchronized static void cleanExpireData (){
		List<Long> removeList = new ArrayList<>();
		for (Map.Entry<Long, List<String>> entry : expireLineMap.entrySet()){
			Long key = entry.getKey();
			if(key<System.currentTimeMillis()){
				entry.getValue().forEach(cacheKey->{
					dataMap.remove(cacheKey);
				});
				removeList.add(key);
			}
		}
		removeList.forEach(item->{
			expireLineMap.remove(item);
		});

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

	
}
