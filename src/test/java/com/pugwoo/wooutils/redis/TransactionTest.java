package com.pugwoo.wooutils.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = {"classpath:applicationContext-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class TransactionTest {

	@Autowired
	private  RedisHelper redisHelper;

	@Test
	public void test() throws Exception {
		final String nameSpace = "myname";
		final String key = "key";

		for(int i=0;i<10;i++){
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					// 同一时刻只有一个人可以拿到lock，返回true
					String lockUuid = redisHelper.requireLock(nameSpace, key, 10);
					if(lockUuid != null){
						System.out.println(Thread.currentThread().getName() + "拿到锁");
					}else{
						System.out.println(Thread.currentThread().getName() + "没有拿到锁，等待....");
					}
					if(lockUuid == null){
						while (lockUuid == null){
							lockUuid = redisHelper.requireLock(nameSpace, key, 10);
						}
						System.out.println(Thread.currentThread().getName() + "等待后拿到锁"+System.currentTimeMillis());
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					boolean succ = redisHelper.releaseLock(nameSpace,key,lockUuid);

					System.out.println(Thread.currentThread().getName() + "释放锁,成功:" + succ);
				}
			});
			thread.start();
		}

		System.out.println("main end");
	}

	
}
