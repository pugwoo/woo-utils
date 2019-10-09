package com.pugwoo.wooutils.redis.sync;

import org.springframework.stereotype.Service;

import com.pugwoo.wooutils.redis.Synchronized;

@Service
public class HelloService {
	
	@Synchronized(namespace = "hello", keyScript = "args[0]")
	public String hello(String name, int i) throws Exception {

		if(i >= 0) {
			System.out.println("hello(" + name + i + ") will sleep 1 seconds");
			Thread.sleep(1000);
			System.out.println("sleep done");

			hello(name, -i);

			return "hello(-)";
		} else {
			// 测试递归场景
			System.out.println("这是递归进来的:name:" + name + ",i:" + i);
			return "";
		}

	}

}
