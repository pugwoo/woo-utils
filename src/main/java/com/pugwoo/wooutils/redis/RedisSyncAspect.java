package com.pugwoo.wooutils.redis;

import java.applet.AppletContext;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
@Aspect
public class RedisSyncAspect implements ApplicationContextAware, InitializingBean {
	
	private ApplicationContext applicationContext;
	
	private RedisHelper redisHelper;

	@Around("@annotation(com.pugwoo.wooutils.redis.Synchronized) execution(* *.*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable{
		System.out.println("--------around before--------");
        Object result = pjp.proceed();
        System.out.println("--------around after--------");
        return result;
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
