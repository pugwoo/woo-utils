package com.pugwoo.wooutils.cache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
    public void benchmark() throws Exception {

        withCacheDemoService.getSomethingWithCache();

        int times = 1000000;
        // 测试调用100万次的时间
        long start = System.currentTimeMillis();
        for(int i = 0; i < times; i++) {
            withCacheDemoService.getSomethingWithCache();
            // System.out.println("i:" + i);
        }
        long end = System.currentTimeMillis();

        System.out.println("cost:" + (end - start) + "ms");
        System.out.println("qps:" + times / ((end - start) / 1000.0));

    }

}
