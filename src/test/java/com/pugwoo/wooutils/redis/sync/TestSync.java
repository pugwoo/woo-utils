package com.pugwoo.wooutils.redis.sync;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.pugwoo.wooutils.redis.RedisSyncContext;

@ContextConfiguration(locations = {"classpath:applicationContext-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class TestSync {

	@Autowired
	private HelloService helloService;
	
	@Test
	public void test() throws Exception {
		for(int i = 1; i <= 10; i++) {
			final int a = i;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						while (true) {
							helloService.hello("nick", a);
							System.out.println("线程" + a +
									"执行结果详情: 是否执行了方法:" + RedisSyncContext.getHaveRun());
							if(RedisSyncContext.getHaveRun()) {
								break;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
		
		Thread.sleep(60000); // 因为在test中，主线程要sleep足够长时间，让其它线程跑完
	}
	
}
