package com.pugwoo.wooutils.redis;

import com.pugwoo.wooutils.collect.ListUtils;
import com.pugwoo.wooutils.json.JSON;
import com.pugwoo.wooutils.redis.impl.JsonRedisObjectConverter;
import com.pugwoo.wooutils.redis.impl.RedisHelperImpl;
import org.junit.Assert;
import org.junit.Test;
import redis.clients.jedis.ScanResult;

import java.math.BigDecimal;
import java.util.*;

public class TestRedisHelper {
	
	public static RedisHelper getRedisHelper() {
		RedisHelperImpl redisHelper = new RedisHelperImpl();
		redisHelper.setHost("192.168.0.112");
		redisHelper.setPort(6379);
		redisHelper.setPassword("devdev");
		redisHelper.setDatabase(0);

		IRedisObjectConverter redisObjectConverter = new JsonRedisObjectConverter();
		redisHelper.setRedisObjectConverter(redisObjectConverter);
		
		return redisHelper;
	}

	@Test
	public void test0() {
		RedisHelper redisHelper = getRedisHelper();
		System.out.println(redisHelper.isOk());
	}

	@Test
	public void testRename() {
		RedisHelper redisHelper = getRedisHelper();
		String oldKey = UUID.randomUUID().toString();
		String newKey = UUID.randomUUID().toString();
		String value = UUID.randomUUID().toString();
		redisHelper.setString(oldKey, 1000, value);
		assert redisHelper.getString(oldKey).equals(value);

		redisHelper.rename(oldKey, newKey);
		assert redisHelper.getString(oldKey) == null;
		assert redisHelper.getString(newKey).equals(value);
	}
	
	@Test
	public void test1() {
		RedisHelper redisHelper = getRedisHelper();
		String key = "mytest" + UUID.randomUUID().toString();
		String value = UUID.randomUUID().toString();
		redisHelper.setString(key, 1000, value);
		String value2 = redisHelper.getString(key);
	    Assert.assertTrue(value.equals(value2));
	}
	
	@Test
	public void test2() {
		RedisHelper redisHelper = getRedisHelper();
		String key = "mytest" + UUID.randomUUID().toString();
		boolean result1 = redisHelper.setStringIfNotExist(key, 60, "you");
		boolean result2 = redisHelper.setStringIfNotExist(key, 60, "you");
		boolean result3 = redisHelper.setStringIfNotExist(key, 60, "you");
		Assert.assertTrue(result1);
		Assert.assertFalse(result2);
		Assert.assertFalse(result3);
	}
	
	@Test
	public void testPipeline() {
		RedisHelper redisHelper = getRedisHelper();
		List<Object> objs = redisHelper.executePipeline(pipeline -> {
			pipeline.set("hello", "world");
			pipeline.get("hello");

		});
		for(Object obj : objs) {
			System.out.println(obj);
		}
	}
	
	@Test
	public void test4() {
		RedisHelper redisHelper = getRedisHelper();
		String key = UUID.randomUUID().toString();
		List<Object> objs = redisHelper.executeTransaction(transaction -> {
			transaction.set(key, "hello");
			transaction.get(key);
		}, key);
		for(Object obj : objs) {
			System.out.println(obj);
		}
	}

	@Test
	public void testScan() {
	    RedisHelper redisHelper = getRedisHelper();
        ScanResult<String> keys = redisHelper.getKeys(null, "*", 1);
        for(String key : keys.getResult()) {
            System.out.println(key);
        }

        System.out.println("===================");

        keys = redisHelper.getKeys(keys.getStringCursor(), "*", 1);
        for(String key : keys.getResult()) {
            System.out.println(key);
        }

    }

    @Test
    public void test3() {
        RedisHelper redisHelper = getRedisHelper();
        Student student = new Student();
        student.setId(3L);
        student.setName("nick");
        student.setBirth(new Date());
        student.setScore(ListUtils.newArrayList(BigDecimal.ONE,
                new BigDecimal(99), new BigDecimal("33.333")));
        List<Student> list = new ArrayList<>();
        list.add(student);
        redisHelper.setObject("just-test111", 1000, list);
        List<Student> list2 = redisHelper.getObject("just-test111", List.class, Student.class);
        System.out.println(list2.get(0).getName());

        Map<Integer, Student> map = new HashMap<>();
        map.put(1, student);
        redisHelper.setObject("just-test333", 1000, map);
        Map<Integer, Student> map2 = redisHelper.getObject("just-test333", Map.class, Integer.class, Student.class);
        System.out.println(map2.get(1).getId());
    }


	public static void main(String[] args) {
		RedisHelper redisHelper = getRedisHelper();
		System.out.println(redisHelper.setStringIfNotExist("hi", 60, "you"));
		System.out.println(redisHelper.setStringIfNotExist("hi", 60, "you"));
		System.out.println(redisHelper.setStringIfNotExist("hi", 60, "you"));
		
		Student student = new Student();
		student.setId(3L);
		student.setName("nick");
		student.setBirth(new Date());
		student.setScore(ListUtils.newArrayList(BigDecimal.ONE,
				new BigDecimal(99), new BigDecimal("33.333")));
		
		redisHelper.setObject("student", 3600, student);
		Student fromRedis = redisHelper.getObject("student", Student.class);
		System.out.println(JSON.toJson(fromRedis));
	}
}
