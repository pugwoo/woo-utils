package com.pugwoo.wooutils.cache;

import com.pugwoo.wooutils.collect.ListUtils;
import com.pugwoo.wooutils.lang.DateUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WithCacheDemoService {

    public String getSomething() throws Exception {
        Thread.sleep(3000);
        return "hello";
    }

    @HiSpeedCache(continueFetchSecond = 60)
    public String getSomethingWithCache() throws Exception {
        Thread.sleep(3000);
        System.out.println("String getSomethingWithCache is executed @ " + DateUtils.format(new Date()));
        return null; // 缓存null值
    }

    @HiSpeedCache(continueFetchSecond = 10, cloneReturn = true, keyScript = "args[0]")
    public Date getSomethingWithCacheCloneReturn(String name) throws Exception {
        Thread.sleep(3000);
        return new Date();
    }

    @HiSpeedCache(continueFetchSecond = 10, useRedis = true, genericClass1 = Date.class)
    public List<Date> getSomethingWithRedis() throws Exception {
        Thread.sleep(3000);
        return ListUtils.newArrayList(new Date(), new Date());
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
