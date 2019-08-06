package com.pugwoo.wooutils.cache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@ContextConfiguration(locations = {"classpath:applicationContext-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class BenchmarkHiSpeedCache {

    @Autowired
    private WithCacheDemoService withCacheDemoService;


    @Test
    public void benchmark() throws Exception {

        withCacheDemoService.getSomethingWithCache();

        int times = 1000000;
        // 测试调用100万次的时间
        long start = System.currentTimeMillis();
        for(int i = 0; i < times; i++) {
            withCacheDemoService.getSomethingWithCache();
        }
        long end = System.currentTimeMillis();

        System.out.println("cost:" + (end - start) + "ms");
        System.out.println("qps:" + times / ((end - start) / 1000.0));
    }


    @Test
    public void benchmarkRedis() throws Exception {

        withCacheDemoService.getSomethingWithRedis();

        int times = 10000;
        // 测试调用1万次的时间
        long start = System.currentTimeMillis();
        for(int i = 0; i < times; i++) {
            withCacheDemoService.getSomethingWithRedis();
            System.out.println(i);
        }
        long end = System.currentTimeMillis();

        System.out.println("cost:" + (end - start) + "ms");
        System.out.println("qps:" + times / ((end - start) / 1000.0));
    }

    // 测试泛型的情况
    @Test
    public void benchmarkClone() throws Exception {
        Map<String, Date> map = withCacheDemoService.getSomeDateWithCache2();
        Date date = map.get("11");

        int times = 100000;
        // 测试调用10万次的时间
        long start = System.currentTimeMillis();
        for(int i = 0; i < times; i++) {
            Map<String, Date> map2 = withCacheDemoService.getSomeDateWithCache2();
            Date date2 = map.get("11");
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
                            // redis大概是2万多的qps
                            withCacheDemoService.getSomethingWithRedis();
                            succ.addAndGet(1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        int second = 600;
        Thread.sleep(second * 1000);
        System.out.println("成功次数:" + succ.get());
        System.out.println("qps:" + succ.get() * 1.0 / second);

        /**
         * 成功次数:73923721794
         * qps:5133591.855763889
         */
    }
}
