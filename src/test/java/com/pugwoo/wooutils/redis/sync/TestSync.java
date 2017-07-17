package com.pugwoo.wooutils.redis.sync;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = {"classpath*:applicationContext-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class TestSync {

	@Autowired
	private HelloService helloService;
	
	@Test
	public void test() throws Exception {
		helloService.hello();
	}
	
}
