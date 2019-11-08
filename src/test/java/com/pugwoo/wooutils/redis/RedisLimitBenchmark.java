package com.pugwoo.wooutils.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Vector;

/**
 * 优化前:
 * 并发数:1,QPS:8022
 * 并发数:10,QPS:1840
 * 并发数:100,QPS:228
 * 并发数:1000,QPS:17
 * 
 * 优化后:
 * 并发数:1,QPS:38899
 * 并发数:10,QPS:83531
 * 并发数:100,QPS:64746
 * 并发数:1000,QPS:51715
 * @author nick
 */
@ContextConfiguration(locations = {"classpath:applicationContext-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class RedisLimitBenchmark {

	@Autowired
	private  RedisHelper redisHelper;

	@Test
	public void test() throws Exception {
		// 一个redisLimitParam 相当于是一个业务配置，例如每分钟只能请求1000次
		final RedisLimitParam redisLimitParam = new RedisLimitParam();
		redisLimitParam.setNamespace("VISIT-LIMIT"); // 每个业务单独设置，每个业务不同
		redisLimitParam.setLimitPeroid(RedisLimitPeroidEnum.DAY); // 设置长一点，方便benchmark
		redisLimitParam.setLimitCount(100000000); // 设置足够大，抢不完

		int concurrents = 100; // 并发数

		long start = System.currentTimeMillis();
		final Vector<Long> vector = new Vector<Long>();
		for(int i = 0; i < concurrents; i++) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					while(true) {
						// 不停抢
						long count = redisHelper.useLimitCount(redisLimitParam, "192.168.2.3");
						if(count > 0) {
							vector.add(1L);
						} else {break;}
					};
				}
			});
			thread.setDaemon(true);
			thread.start();
		}

		Thread.sleep(60000); // 测试将持续60秒
		long end = System.currentTimeMillis();

		System.out.println("并发数:" + concurrents +
				",QPS:" + (int)((vector.size() * 1000.0 / (end - start))));
	}

}
