package com.pugwoo.wooutils.redis;

public class TestRedisHelper {
	
	public static RedisHelper getRedisHelper() {
		RedisHelperImpl redisHelper = new RedisHelperImpl();
		redisHelper.setHost("127.0.0.1");
		redisHelper.setPort(6379);
		redisHelper.setPassword("");
		
		return redisHelper;
	}

	public static void main(String[] args) {
		RedisHelper redisHelper = getRedisHelper();
		System.out.println(redisHelper.setStringIfNotExist("hi", 60, "you"));
		System.out.println(redisHelper.setStringIfNotExist("hi", 60, "you"));
		System.out.println(redisHelper.setStringIfNotExist("hi", 60, "you"));
	}
}
