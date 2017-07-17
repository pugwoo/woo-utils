package com.pugwoo.wooutils.redis.sync;

import org.springframework.stereotype.Service;

import com.pugwoo.wooutils.redis.Synchronized;

@Service
public class HelloService {

	@Synchronized(namespace = "hello")
	public void hello() {
		System.out.println("hello()");
	}
	
}
