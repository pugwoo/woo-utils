package com.pugwoo.wooutils.redis;

import com.pugwoo.wooutils.redis.impl.RedisHelperImpl;

public class TestAutoIncrementId {

	public static void main(String[] args) {
		RedisHelperImpl redisHelper = new RedisHelperImpl();
        redisHelper.setHost("127.0.0.1");
        redisHelper.setPort(6379);
        redisHelper.setPassword("");
        
        long start = System.currentTimeMillis();
        for(int i = 0; i < 10000; i++) {
            Long id = redisHelper.getAutoIncrementId("ORDER");
            System.out.println(id);
        }
        long end = System.currentTimeMillis();
        System.out.println("cost:" + (end - start) + "ms");
	}
	
}
