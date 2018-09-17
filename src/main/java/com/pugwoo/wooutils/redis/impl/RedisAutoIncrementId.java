package com.pugwoo.wooutils.redis.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pugwoo.wooutils.redis.RedisHelper;

public class RedisAutoIncrementId {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisAutoIncrementId.class);

	public static Long getAutoIncrementId(RedisHelper redisHelper, String namespace) {
		
		if(namespace == null || namespace.isEmpty()) {
			LOGGER.error("getAutoIncrementId with error params: namespace:{}",
					namespace, new Exception());
			return null;
		}
		
		return redisHelper.execute(jedis -> {
			try {
				return jedis.incr(namespace + "_ID");
			} catch (Exception e) {
				LOGGER.error("getAutoIncrementId jedis incr error, namespace:{}", namespace, e);
				return null;
			}
		});
	}
	
}
