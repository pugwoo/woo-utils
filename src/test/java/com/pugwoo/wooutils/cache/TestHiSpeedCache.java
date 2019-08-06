package com.pugwoo.wooutils.cache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;

@ContextConfiguration(locations = {"classpath:applicationContext-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class TestHiSpeedCache {

    @Autowired
    private WithCacheDemoService withCacheDemoService;

    @Test
    public void test() throws Exception {
        long start = System.currentTimeMillis();
        String str = withCacheDemoService.getSomething();
        System.out.println(str + new Date());
        str = withCacheDemoService.getSomething();
        System.out.println(str + new Date());
        str = withCacheDemoService.getSomething();
        System.out.println(str + new Date());
        long end = System.currentTimeMillis();

        System.out.println("cost:" + (end - start) + "ms");
        assert (end- start) > 9000;
    }

    @Test
    public void test2() throws Exception {
        long start = System.currentTimeMillis();
        String str = withCacheDemoService.getSomethingWithCache();
        System.out.println(str + new Date());
        str = withCacheDemoService.getSomethingWithCache();
        System.out.println(str + new Date());
        str = withCacheDemoService.getSomethingWithCache();
        System.out.println(str + new Date());
        long end = System.currentTimeMillis();

        System.out.println("cost:" + (end - start) + "ms");
        assert (end - start) > 3000 && (end - start) < 3500;
    }

    @Test
    public void test3() throws Exception {
        long start = System.currentTimeMillis();
        Date date = withCacheDemoService.getSomethingWithCacheCloneReturn("hello");
        System.out.println(date + "," + new Date());
        date = withCacheDemoService.getSomethingWithCacheCloneReturn("hello");
        System.out.println(date + "," + new Date());
        date = withCacheDemoService.getSomethingWithCacheCloneReturn("hello");
        System.out.println(date + "," + new Date());
        long end = System.currentTimeMillis();

        System.out.println("cost:" + (end - start) + "ms");
        assert (end - start) > 3000 && (end - start) < 3500;
    }

    @Test
    public void test4() throws Exception {
        long start = System.currentTimeMillis();
        List<Date> dates = withCacheDemoService.getSomethingWithRedis();
        System.out.println(dates + "," + new Date());
        dates = withCacheDemoService.getSomethingWithRedis();
        System.out.println(dates + "," + new Date());
        dates = withCacheDemoService.getSomethingWithRedis();
        assert dates.get(0) != null && dates.get(0) instanceof Date;
        System.out.println(dates + "," + new Date());
        long end = System.currentTimeMillis();

        System.out.println("cost:" + (end - start) + "ms");
        assert (end - start) > 3000 && (end - start) < 3500;
    }

}
