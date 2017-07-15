package com.pugwoo.wooutils.redis;

import java.util.List;
import java.util.Vector;

import com.pugwoo.wooutils.redis.impl.RedisHelperImpl;

/**
 * 普通笔记本本地redis(windows上)压测结果：
 * 
 * 并发数:1,QPS:7603
 * 并发数:10,QPS:14021
 * 并发数:100,QPS:13744
 * 并发数:200,QPS:10311
 * 并发数:300,QPS:8907
 * 并发数:500,QPS:6603
 * 并发数:1000,QPS:3661
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
					RedisHelperImpl redisHelper = new RedisHelperImpl();
			        redisHelper.setHost("127.0.0.1");
			        redisHelper.setPort(6379);
			        redisHelper.setPassword("");
			        while(true) {
						Long id = redisHelper.getAutoIncrementId("ORDER");
						ids.add(id);
			        }
				}
			});
        	thread.setDaemon(true);
        	thread.start();
        }
        
        int ms = 60000;
        Thread.sleep(ms); // 测试将持续60秒
        long end = System.currentTimeMillis();
        
        System.out.println("并发数:" + concurrents +
        		",QPS:" + (int)((ids.size() * 1000.0 / (end - start))));
	}
	
}
