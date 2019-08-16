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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.lang.reflect.Method;
import java.util.Map;

@EnableAspectJAutoProxy
@Aspect
public class RedisSyncAspect implements ApplicationContextAware, InitializingBean {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisSyncAspect.class);
	
	private ApplicationContext applicationContext;
	
	private RedisHelper redisHelper;

	@Around("@annotation(com.pugwoo.wooutils.redis.Synchronized) execution(* *.*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
		if(this.redisHelper == null) {
			LOGGER.error("redisHelper is null, RedisSyncAspect will passthrough all method call");
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
			boolean requireLock = redisHelper.requireLock(namespace, key, expireSecond);
			if(requireLock) {
				try {
					RedisSyncContext.set(true, true);
					return pjp.proceed();
				} finally {
					redisHelper.releaseLock(namespace, key);
				}
			}
			
			if(waitLockMillisecond == 0) {
				RedisSyncContext.set(true, false);
				return null;
			}
			long totalWait = System.currentTimeMillis() - start;
			if(totalWait >= waitLockMillisecond) {
				RedisSyncContext.set(true, false);
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

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if(this.redisHelper == null) { // 尝试从spring容器中拿
			if(this.applicationContext != null) {
				RedisHelper rh = this.applicationContext.getBean(RedisHelper.class);
				if(rh != null) {
					this.redisHelper = rh;
				}
			}
		}
	}
	
}
