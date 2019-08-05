package com.pugwoo.wooutils.cache;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WithCacheDemoService {

    public String getSomething() throws Exception {
        Thread.sleep(3000);
        return "hello";
    }

    @HiSpeedCache(continueFetchSecond = 10)
    public String getSomethingWithCache() throws Exception {
        Thread.sleep(3000);
        return "hello";
    }

    @HiSpeedCache(continueFetchSecond = 10, cloneReturn = true)
    public Date getSomethingWithCacheCloneReturn(String name) throws Exception {
        Thread.sleep(3000);
        return new Date();
    }

    // 支持克隆情况下的泛型
    @HiSpeedCache(continueFetchSecond = 10, cloneReturn = true, genericClass1 = Date.class)
    public List<Date> getSomeDateWithCache() throws Exception {
        Thread.sleep(3000);
        List<Date> dates = new ArrayList<>();
        dates.add(new Date());
        dates.add(new Date());
        dates.add(new Date());
        return dates;
    }

    // 支持克隆情况下的泛型
    @HiSpeedCache(continueFetchSecond = 10, cloneReturn = true,
            genericClass1 = String.class, genericClass2 = Date.class)
    public Map<String, Date> getSomeDateWithCache2() throws Exception {
        Thread.sleep(3000);
        Map<String, Date> map = new HashMap<>();
        map.put("11", new Date());
        map.put("22", new Date());
        map.put("33", new Date());
        return map;
    }
}
