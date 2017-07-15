package com.pugwoo.wooutils.redis;

public class TransactionTest {

	public static void main(String[] args) {
		
		final RedisHelper redisHelper = TestRedisHelper.getRedisHelper();
		
		String myNamespace = "MY-NAMESPACE";
		
		boolean lock = redisHelper.requireLock(myNamespace, "1234", 10);
		System.out.println(lock);
		
		// 可以尝试把这个main程序跑2次，第一次可以拿到锁，第二次不行，要过10秒事务超时了，就又可以获得了。
	}
	
}
