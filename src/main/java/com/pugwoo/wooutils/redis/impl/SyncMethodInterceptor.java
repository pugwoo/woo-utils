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
	
	public SyncMethodInterceptor(RedisHelper redisHelper) {
		this.redisHelper = redisHelper;
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		
		System.out.println("hello world");
		
		return null;
	}

}
