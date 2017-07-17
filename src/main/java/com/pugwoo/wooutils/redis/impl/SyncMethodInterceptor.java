package com.pugwoo.wooutils.redis.impl;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.pugwoo.wooutils.redis.RedisHelper;

/**
 * 分布式锁
 * @author nick
 */
public class SyncMethodInterceptor implements MethodInterceptor {
	
	private RedisHelper redisHelper;
	
	public SyncMethodInterceptor() {
	}
	
	public SyncMethodInterceptor(RedisHelper redisHelper) {
		this.redisHelper = redisHelper;
	}
	
	public RedisHelper getRedisHelper() {
		return redisHelper;
	}

	public void setRedisHelper(RedisHelper redisHelper) {
		this.redisHelper = redisHelper;
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		
		System.out.println("sync invoker");
		
		return invocation.proceed();
	}

}
