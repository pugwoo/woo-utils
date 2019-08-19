package com.pugwoo.wooutils.redis.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pugwoo.wooutils.redis.RedisHelper;

import java.util.UUID;

/**
 * @author nick
 * redis锁，用于保证分布式系统同一时刻只有一个程序获得资源。
 *          对于指定的nameSpace，每次只有一个对象可以获得锁。
 * redis有个很好的特性，就是超时删除。非常合适在实际的项目场景中。
 * 
 * 【注意】分布式锁并不等同于分布式事务。
 *        我建议应尽量避免使用到分布式事务，应由分布式各系统自行进行数据修正工作。
 */
public class RedisLock {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisLock.class);
	
	private static String getKey(String namespace, String key) {
		return namespace + ":" + key;
	}

	/**
	 * 获得一个名称为key的锁，redis保证同一时刻只有一个client可以获得锁。
	 * 
	 * @param namespace 命名空间，每个应用独立的空间
	 * @param key 业务key，redis将保证同一个namespace同一个key只有一个client可以拿到锁
	 * @param maxTransactionSeconds 单位秒，必须大于0,拿到锁之后,预计多久可以完成这个事务，如果超过这个时间还没有归还锁，那么事务将失败
	 * @return 如果加锁成功，返回锁的唯一识别字符，可用于解锁；如果加锁失败，则返回null
	 */
	public static String requireLock(RedisHelper redisHelper, String namespace,
			String key, int maxTransactionSeconds) {
		if(namespace == null || key == null || key.isEmpty() || maxTransactionSeconds <= 0) {
			LOGGER.error("requireLock with error params: namespace:{},key:{},maxTransactionSeconds:{}",
					namespace, key, maxTransactionSeconds, new Exception());
			return null;
		}
		
		try {
			String newKey = getKey(namespace, key);
			String uuid = UUID.randomUUID().toString();
			boolean result = redisHelper.setStringIfNotExist(newKey, maxTransactionSeconds, uuid);
			return result ? uuid : null;
		} catch (Exception e) {
			LOGGER.error("requireLock error, namespace:{}, key:{}", namespace, key, e);
			return null;
		}
	}
	
	/**
	 * 如果事务已经完成，则归还锁。
	 * @param namespace
	 * @param key
	 * @param lockUuid 锁的uuid，必须提供正确的uuid才可以解锁
	 * @return 解锁成功返回true，失败返回false
	 */
	public static boolean releaseLock(RedisHelper redisHelper, String namespace, String key, String lockUuid) {
		if(namespace == null || key == null || key.isEmpty()) {
			LOGGER.error("requireLock with error params: namespace:{},key:{}",
					namespace, key, new Exception());
			return false;
		}
		
		try {
			String newKey = getKey(namespace, key);
			String value = redisHelper.getString(newKey);
			if(value == null) {
				LOGGER.warn("releaseLock namespace:{}, key:{}, lock not exist", namespace, key);
				return true;
			} else if (value.equals(lockUuid)) {
				redisHelper.remove(newKey);
				return true;
			} else {
				LOGGER.error("releaseLock namespace:{}, key:{} fail, uuid not match, redis:{}, given:{}",
						namespace, key, value, lockUuid);
				return false;
			}
		} catch (Exception e) {
			LOGGER.error("releaseLock error, namespace:{}, key:{}", namespace, key, e);
			return false;
		}
	}
	
}
