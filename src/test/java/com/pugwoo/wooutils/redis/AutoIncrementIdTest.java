package com.pugwoo.wooutils.redis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.pugwoo.wooutils.redis.impl.RedisHelperImpl;

public class AutoIncrementIdTest {

	public static void main(String[] args) throws InterruptedException {
		final RedisHelperImpl redisHelper = new RedisHelperImpl();
        redisHelper.setHost("127.0.0.1");
        redisHelper.setPort(6379);
        redisHelper.setPassword("");
        redisHelper.setMaxConnection(1000);
        
        final List<Long> ids = new Vector<Long>();
        
        long start = System.currentTimeMillis();
        List<Thread> threads = new ArrayList<Thread>();
        for(int t = 0; t < 900; t++) { // redis最大连接数一般到900，受服务器打开文件数限制，需要修改
        	Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
			        for(int i = 0; i < 100; i++) {
			            Long id = redisHelper.getAutoIncrementId("ORDER");
			     //       System.out.println(id);
			            ids.add(id);
			        }
				}
			});
        	thread.start();
        	threads.add(thread);
        }
        
        for(Thread thread : threads) {
        	thread.join();
        }

        long end = System.currentTimeMillis();
        System.out.println("cost:" + (end - start) + "ms");
        
        // 校验正确性
        Set<Long> set = new HashSet<Long>();
        boolean isDup = false;
        boolean hasNull = false;
        for(Long id : ids) {
        	if(id == null) {
        		hasNull = true;
        		continue;
        	}
        	if(set.contains(id)) {
        		isDup = true;
        	} else {
        		set.add(id);
        	}
        }
        System.out.println(isDup ? "数据错误，有重复" : "数据正确");
        System.out.println(hasNull ? "数据错误，有null" : "数据正确");
	}
	
}
