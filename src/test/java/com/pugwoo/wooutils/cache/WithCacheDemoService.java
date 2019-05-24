package com.pugwoo.wooutils.cache;

import org.springframework.stereotype.Service;

@Service
public class WithCacheDemoService {

    public String getSomething() throws Exception {
        Thread.sleep(3000);
        return "hello";
    }

    @HiSpeedCache(continueFetchSecond = 10)
    public String getSomethingWithCache() throws Exception {
      //  System.out.println("getSomethingWithCache() is called" + new Date());
        Thread.sleep(3000);
      //  System.out.println("getSomethingWithCache() call end" + new Date());
        return "hello";
    }

    @HiSpeedCache
    public String getSomethingWithCache(String name) throws Exception {
        Thread.sleep(3000);
        return "hello";
    }
}
