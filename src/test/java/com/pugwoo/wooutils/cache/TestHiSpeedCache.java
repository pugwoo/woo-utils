package com.pugwoo.wooutils.cache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.atomic.AtomicLong;

@ContextConfiguration(locations = {"classpath:applicationContext-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class TestHiSpeedCache {

    @Autowired
    private WithCacheDemoService withCacheDemoService;

    @Test
    public void test() throws Exception {
        long start = System.currentTimeMillis();
        withCacheDemoService.getSomething();
        withCacheDemoService.getSomething();
        withCacheDemoService.getSomething();
        long end = System.currentTimeMillis();

        System.out.println("cost:" + (end - start) + "ms");
    }

    @Test
    public void test2() throws Exception {
        long start = System.currentTimeMillis();
        withCacheDemoService.getSomethingWithCache();
        withCacheDemoService.getSomethingWithCache();
        withCacheDemoService.getSomethingWithCache();
        long end = System.currentTimeMillis();

        System.out.println("cost:" + (end - start) + "ms");

        start = System.currentTimeMillis();
        withCacheDemoService.getSomethingWithCache("hello");
        withCacheDemoService.getSomethingWithCache("world");
        withCacheDemoService.getSomethingWithCache("you");
        end = System.currentTimeMillis();

        System.out.println("cost:" + (end - start) + "ms");
    }

    @Test
    public void testContinue() throws Exception {
        withCacheDemoService.getSomethingWithCache();

        Thread.sleep(30000);
    }

    @Test
    public void benchmark() throws Exception {

        withCacheDemoService.getSomethingWithCache();

        int times = 10000000;
        // 测试调用1000万次的时间
        long start = System.currentTimeMillis();
        for(int i = 0; i < times; i++) {
            withCacheDemoService.getSomethingWithCache();
            // System.out.println("i:" + i);
        }
        long end = System.currentTimeMillis();

        System.out.println("cost:" + (end - start) + "ms");
        System.out.println("qps:" + times / ((end - start) / 1000.0));
    }

    @Test
    public void testMultiThread() throws Exception {
        final AtomicLong succ = new AtomicLong(0);

        for(int thread = 0; thread < 200; thread++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < 1000000000; i++) {
                        try {
                            withCacheDemoService.getSomethingWithCache();
                            succ.addAndGet(1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        int second = 60;
        Thread.sleep(second * 1000);
        System.out.println("成功次数:" + succ.get());
        System.out.println("qps:" + succ.get() * 1.0 / second);

        /**
         * 成功次数:73923721794
         * qps:5133591.855763889
         */
    }

}
