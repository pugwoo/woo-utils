package com.pugwoo.wooutils.redis;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

public class RedisUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisUtils.class);

	/**
	 * CAS，成功返回true，失败返回false
	 * @param jedis jedis执行后，【不会】被关闭，由使用方自己来关闭。
	 * @param expireSeconds 超时时间，如果是null，则不设置
	 */
	public static boolean compareAndSet(Jedis jedis, String key, String value, String oldValue,
			Integer expireSeconds) {
		try {
			jedis.watch(key);
			String readOldValue = jedis.get(key);
			if(Objects.equals(readOldValue, oldValue)) {
				Transaction tx = jedis.multi();
				Response<String> result = null;
				if(expireSeconds != null) {
					result = tx.setex(key, expireSeconds, value);
				} else {
					result = tx.set(key, value);
				}

				List<Object> results = tx.exec();
				if(results == null || result == null || result.get() == null) {
					return false;
				} else {
					return true;
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			LOGGER.error("compareAndSet error,key:{}, value:{}, oldValue:{}", key, value, oldValue);
			return false;
		}
	}
	
}
