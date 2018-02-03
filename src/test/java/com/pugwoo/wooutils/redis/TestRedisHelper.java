package com.pugwoo.wooutils.redis;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.pugwoo.wooutils.collect.ListUtils;
import com.pugwoo.wooutils.json.JSON;
import com.pugwoo.wooutils.redis.impl.JsonRedisObjectConverter;
import com.pugwoo.wooutils.redis.impl.RedisHelperImpl;

public class TestRedisHelper {
	
	public static RedisHelper getRedisHelper() {
		RedisHelperImpl redisHelper = new RedisHelperImpl();
		redisHelper.setHost("127.0.0.1");
		redisHelper.setPort(6379);
		redisHelper.setPassword("");
		
		IRedisObjectConverter redisObjectConverter = new JsonRedisObjectConverter();
		redisHelper.setRedisObjectConverter(redisObjectConverter);
		
		return redisHelper;
	}
	
	static class Student {
		@SuppressWarnings("unused")
		private Long id;
		@SuppressWarnings("unused")
		private String name;
		@SuppressWarnings("unused")
		private Date birth;
		@SuppressWarnings("unused")
		private List<BigDecimal> score;
		
		// 对于jackson field序列化  setter也不是必须的
		public void setId(Long id) {
			this.id = id;
		}
		public void setName(String name) {
			this.name = name;
		}
		public void setBirth(Date birth) {
			this.birth = birth;
		}
		public void setScore(List<BigDecimal> score) {
			this.score = score;
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
		System.out.println(JSON.toJson(fromRedis)); // 这里会输出{}，因为没有getter，得用debug看数据
	}
}
