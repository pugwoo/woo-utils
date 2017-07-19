package com.pugwoo.wooutils.redis;

import java.util.List;
import java.util.Vector;

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
public class AutoIncrementIdBenchmark {

	public static void main(String[] args) throws Exception {
        final List<Long> ids = new Vector<Long>();
        long start = System.currentTimeMillis();
        int concurrents = 1000; // 并发数
        for(int i = 0; i < concurrents; i++) {
        	Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {			        
			        RedisHelper redisHelper = TestRedisHelper.getRedisHelper();
			        while(true) {
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
	}
	
}
