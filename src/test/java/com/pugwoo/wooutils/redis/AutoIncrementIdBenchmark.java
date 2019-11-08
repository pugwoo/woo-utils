package com.pugwoo.wooutils.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 普通台式机本地redis(windows上)压测结果：
 * 
 * 并发数:1,QPS:40719
 * 并发数:10,QPS:86976
 * 并发数:100,QPS:65050
 * 并发数:200,QPS:47124
 * 并发数:300,QPS:38487
 * 并发数:500,QPS:25554
 * 并发数:1000,QPS:12687
 * @author NICK
 *
 */
@ContextConfiguration(locations = {"classpath:applicationContext-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class AutoIncrementIdBenchmark {

	@Autowired
	private  RedisHelper redisHelper;

	@Test
	public void test() throws Exception {
		final List<Long> ids = new Vector<Long>();
		long start = System.currentTimeMillis();

		AtomicBoolean stop = new AtomicBoolean(false);

		int concurrents = 800; // 并发数
		for(int i = 0; i < concurrents; i++) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					while(!stop.get()) {
						Long id = redisHelper.getAutoIncrementId("ORDER");
						ids.add(id);
					}
				}
			});
			thread.setDaemon(true);
			thread.start();
		}

		Thread.sleep(60000); // 测试将持续60秒
		long end = System.currentTimeMillis();

		System.out.println("并发数:" + concurrents +
				",QPS:" + (int)((ids.size() * 1000.0 / (end - start))));

		stop.set(true);
		Thread.sleep(5000); // 等待线程结束

		// 检查ids中是否有重复的
		Set<Long> sets = new HashSet<>();
		sets.addAll(ids);

		assert ids.size() == sets.size();
	}

}
