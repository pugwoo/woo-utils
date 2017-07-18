package com.pugwoo.wooutils.redis.sync;

import org.springframework.stereotype.Service;

import com.pugwoo.wooutils.redis.Synchronized;

@Service
public class HelloService {
	
	@Synchronized(namespace = "hello")
	public String hello(String arg) throws Exception {
		System.out.println("hello(" + arg + ") will sleep 1 seconds");
		Thread.sleep(1000);
		System.out.println("sleep done");
		return "hello(-)";
	}
	
	public String hello2() {
		System.out.println("hello2()");
		return "hello2(-)";
	}
	
}
