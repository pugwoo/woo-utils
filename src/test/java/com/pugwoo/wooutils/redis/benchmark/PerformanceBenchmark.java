package com.pugwoo.wooutils.redis.benchmark;

import com.pugwoo.wooutils.redis.RedisHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.atomic.AtomicLong;

@ContextConfiguration(locations = {"classpath:applicationContext-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class PerformanceBenchmark {

    @Autowired
    private RedisHelper redisHelper;

    /**
     * 家里server+笔记本: 300线程qps9049，200线程qps9000，100线程qps8488，50线程8303，10线程3893
     * 台式+dev：300线程qps12262，200线程qps12503，100线程qps11550，50线程qps8771，10线程2444
     */
    @Test
    public void benchIncr() {
        AtomicLong count = new AtomicLong();

        int seconds = 60;
        int threads = 10;

        for(int i = 0; i < threads; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        redisHelper.execute(jedis -> jedis.incr("a"));
                        count.incrementAndGet();
                    }
                }
            }).start();
        }

        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
        }

        double qps = count.get() * 1.0 / seconds;
        System.out.println("qps:" + qps);

    }
}
