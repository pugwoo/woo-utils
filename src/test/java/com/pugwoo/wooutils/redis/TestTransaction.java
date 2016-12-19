package com.pugwoo.wooutils.redis;

import com.pugwoo.wooutils.redis.transaction.RedisTransaction;

import redis.clients.jedis.Jedis;

public class TestTransaction {

	public static void main(String[] args) {
		String myNamespace = "MY-NAMESPACE";
		
		Jedis jedis = RedisConnectionManager.getJedisConnection();
		
		boolean lock = RedisTransaction.requireLock(jedis, myNamespace, "1234", 10);
		System.out.println(lock);
		
		// 可以尝试把这个main程序跑2次，第一次可以拿到锁，第二次不行，要过10秒事务超时了，就又可以获得了。
		
		// jedis = RedisConnectionManager.getJedisConnection();
		// RedisTransaction.releaseLock(jedis, myNamespace, "1234"); // 加上这行，则主动释锁
	}
	
}
