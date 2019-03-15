package com.pugwoo.wooutils.redis;

import com.pugwoo.wooutils.collect.ListUtils;
import com.pugwoo.wooutils.json.JSON;
import com.pugwoo.wooutils.redis.impl.JsonRedisObjectConverter;
import com.pugwoo.wooutils.redis.impl.RedisHelperImpl;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TestRedisHelper {
	
	public static RedisHelper getRedisHelper() {
		RedisHelperImpl redisHelper = new RedisHelperImpl();
		redisHelper.setHost("192.168.0.101");
		redisHelper.setPort(6379);
		redisHelper.setPassword("123456789");
		redisHelper.setDatabase(1);
		
		IRedisObjectConverter redisObjectConverter = new JsonRedisObjectConverter();
		redisHelper.setRedisObjectConverter(redisObjectConverter);
		
		return redisHelper;
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
