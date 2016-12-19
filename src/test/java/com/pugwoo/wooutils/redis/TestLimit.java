package com.pugwoo.wooutils.redis;

import java.util.Date;
import java.util.Vector;

import com.pugwoo.wooutils.redis.limit.RedisLimit;
import com.pugwoo.wooutils.redis.limit.RedisLimitParam;
import com.pugwoo.wooutils.redis.limit.RedisLimitPeroidEnum;

import redis.clients.jedis.Jedis;

public class TestLimit {

	public static void main(String[] args) throws Exception {
		
		// 一个redisLimitParam 相当于是一个业务配置，例如每分钟只能请求1000次
		final RedisLimitParam redisLimitParam = new RedisLimitParam();
		redisLimitParam.setNamespace("VIEW-LIMIT"); // 每个业务单独设置，每个业务不同
		redisLimitParam.setLimitPeroid(RedisLimitPeroidEnum.MINUTE);
		redisLimitParam.setLimitCount(1000);
		
		final Vector<Long> vector = new Vector<Long>();
		for(int i = 0; i < 100; i++) { // 模拟100个线程
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					long count = 0;
					do {
						Jedis jedis = RedisConnectionManager.getJedisConnection();
						count = RedisLimit.useLimitCount(jedis, redisLimitParam, "192.168.2.3");
						if(count > 0) {
							System.out.println(Thread.currentThread().getName() +
									"抢到了第" + count + "个，时间:" + new Date());
							vector.add(1L);
							try {
								Thread.sleep(2); // 抢到了等2毫秒再抢
							} catch (InterruptedException e) {
							}
						} else {
							System.out.println("抢完了，线程" + Thread.currentThread().getName()
									+ "退出");
							break;
						}
					} while(true);
				}
			}, "线程"+i);
			thread.setDaemon(true);
			thread.start();
		}
		
		Thread.sleep(10000);
		System.out.println("final:" + vector.size()); // 每分钟最多只能拿到1000个
	}
	
}
