package com.pugwoo.wooutils.redis.sync;

import org.springframework.stereotype.Service;

import com.pugwoo.wooutils.redis.Synchronized;

@Service
public class HelloService {
	
	@Synchronized(namespace = "hello", keyScript = "args[0]")
	public String hello(String name, int i) throws Exception {
		System.out.println("hello(" + name + i + ") will sleep 1 seconds");
		Thread.sleep(1000);
		System.out.println("sleep done");
		return "hello(-)";
	}

}
