package com.pugwoo.wooutils.cache;

import org.springframework.stereotype.Service;

@Service
public class WithCacheDemoService {

    public String getSomething() throws Exception {
        Thread.sleep(3000);
        return "hello";
    }

    @HiSpeedCache
    public String getSomethingWithCache() throws Exception {
        Thread.sleep(3000);
        return "hello";
    }

}
