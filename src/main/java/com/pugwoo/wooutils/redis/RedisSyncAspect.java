package com.pugwoo.wooutils.redis;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Map;

@EnableAspectJAutoProxy
@Aspect
public class RedisSyncAspect {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisSyncAspect.class);

	@Autowired
	private RedisHelper redisHelper;

	@PostConstruct
	private void init() {
		if(redisHelper == null) {
			LOGGER.error("redisHelper is null, RedisSyncAspect will pass through all method call");
		} else {
			LOGGER.info("@Synchronized init success.");
		}
	}

	@Around("@annotation(com.pugwoo.wooutils.redis.Synchronized) execution(* *.*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
		if(this.redisHelper == null) {
			LOGGER.error("redisHelper is null, RedisSyncAspect will pass through all method call");
			RedisSyncContext.set(false, true);
			return pjp.proceed();
		}
		
		MethodSignature signature = (MethodSignature) pjp.getSignature();
		Method targetMethod = signature.getMethod();
		Synchronized sync = targetMethod.getAnnotation(Synchronized.class);
		
		String namespace = sync.namespace();
		int expireSecond = sync.expireSecond();
		int waitLockMillisecond = sync.waitLockMillisecond();

		String key = "-";
        String keyScript = sync.keyScript();
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
		
		int a = 0, b = 1; // 构造兔子数列
		
		long start = System.currentTimeMillis();
		while(true) {
			String lockUuid = redisHelper.requireLock(namespace, key, expireSecond);
			if(lockUuid != null) {
				if(sync.logDebug()) {
					LOGGER.info("namespace:{},key:{},got lock,expire second:{},lockUuid:{},threadName:{}",
							namespace, key, expireSecond, lockUuid, Thread.currentThread().getName());
				}
				try {
					RedisSyncContext.set(true, true);
					return pjp.proceed();
				} finally {
					boolean result = redisHelper.releaseLock(namespace, key, lockUuid);
					if(sync.logDebug()) {
						if(result) {
							LOGGER.info("namespace:{},key:{} release lock success, lockUuid:{},threadName:{}",
									namespace, key, lockUuid, Thread.currentThread().getName());
						} else {
							LOGGER.error("namespace:{},key:{} release lock fail, lockUuid:{},threadName:{}",
									namespace, key, lockUuid, Thread.currentThread().getName());
						}
					}
				}
			} else {
				if(sync.logDebug()) {
					LOGGER.info("namespace:{},key:{}, NOT get a lock,threadName:{}", namespace, key,
							Thread.currentThread().getName());
				}
			}
			
			if(waitLockMillisecond == 0) {
				RedisSyncContext.set(true, false);
				if(sync.logDebug()) {
					LOGGER.info("namespace:{},key:{}, give up getting a lock,threadName:{}", namespace, key,
							Thread.currentThread().getName());
				}
				return null;
			}
			long totalWait = System.currentTimeMillis() - start;
			if(totalWait >= waitLockMillisecond) {
				RedisSyncContext.set(true, false);
				if(sync.logDebug()) {
					LOGGER.info("namespace:{},key:{}, give up getting a lock,total wait:{}ms,threadName:{}", namespace, key, totalWait, Thread.currentThread().getName());
				}
				return null;
			}
			if(waitLockMillisecond - totalWait < b) {
				Thread.sleep(waitLockMillisecond - totalWait);
			} else {
				Thread.sleep(b);
				int c = a + b;
				a = b;
				b = c; // 构造兔子数列
				if(b > 1000) {b = 1000;}
			}
		}
    }

	public void setRedisHelper(RedisHelper redisHelper) {
		this.redisHelper = redisHelper;
	}

}
