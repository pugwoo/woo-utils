package com.pugwoo.wooutils.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;
import java.util.Vector;

@ContextConfiguration(locations = {"classpath:applicationContext-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class RedisLimitTest {

	@Autowired
	private  RedisHelper redisHelper;

	@Test
	public void test() throws Exception {
		// 一个redisLimitParam 相当于是一个业务配置，例如每分钟只能请求1000次
		final RedisLimitParam redisLimitParam = new RedisLimitParam();
		redisLimitParam.setNamespace("VIEW-LIMIT"); // 每个业务单独设置，每个业务不同
		redisLimitParam.setLimitPeroid(RedisLimitPeroidEnum.MINUTE);
		redisLimitParam.setLimitCount(1000);

		final Vector<Long> vector = new Vector<Long>();
		List<Thread> threads = new Vector<Thread>();
		for(int i = 0; i < 100; i++) { // 模拟100个线程
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					long count = 0;
					do {
						count = redisHelper.useLimitCount(redisLimitParam, "192.168.2.3");
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
			threads.add(thread);
		}

		for(Thread thread : threads) {
			thread.join();
		}
		System.out.println("final:" + vector.size());
		// 每分钟最多只能拿到1000个，如果执行过程没有跨到2个1分钟内执行，那么总数应该是1000
	}

}
