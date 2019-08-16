package com.pugwoo.wooutils.redis.impl;

import com.pugwoo.wooutils.redis.RedisHelper;
import com.pugwoo.wooutils.redis.RedisLimitParam;
import com.pugwoo.wooutils.redis.RedisLimitPeroidEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 使用redis控制全局的操作次数限制。可用于限制自然单位时间（天、小时、分钟、周等），全局的总操作次数。<br>
 * <br>
 * 注：redis的incr或decr在redis删除某个expire key时，会出现多个线程拿到相同值的情况，所以incr或decr来做这个功能并不可靠。
 * 
 * 2017年7月19日 14:14:19
 * 由于使用redis cas，在高并发情况下会导致大量无效的重试，从而把性能降低到一个不太可能用于线上环境的情况。
 * 这里换了一种实现方式进行优化：使用key+时间的方式作为key，巧妙避免掉跨时段的redis清理key和incr的冲突。
 * 
 * @author pugwoo
 */
public class RedisLimit {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisLimit.class);
	
	/**
	 * 查询key的redis限制剩余次数。
	 * @param redisHelper
	 * @param limitParam 限制参数
	 * @param key 业务主键
	 * @return -1是系统异常，正常值大于等于0
	 */
	public static long getLimitCount(RedisHelper redisHelper, RedisLimitParam limitParam, String key) {
		if(!checkParam(limitParam, key)) {
			return 0;
		}
		
		try {
			key = getKey(limitParam, key);
			String value = redisHelper.getString(key);
			int usedCount = value == null ? 0 : new Integer(value);
			int left = limitParam.getLimitCount() - usedCount;
			return left < 0 ? 0 : left;
		} catch (Exception e) {
			LOGGER.error("getLimitCount error,namespace:{},key:{}",
					limitParam.getNamespace(), key, e);
			return -1;
		}
	}
		
	/**
	 * 使用了count次限制。一般来说，业务都是在处理成功后才扣减使用是否成功的限制，
	 * 如果使用失败了，如果业务支持事务回滚，那么可以回滚掉，此时可以不用分布式锁做全局限制。
	 * 
	 * @param redisHelper
	 * @param limitParam
	 * @param key
	 * @param count 一次可以使用掉多个count
	 * @return 返回是当前周期内第几个(该值会大于0)使用配额的，如果返回-1，表示使用配额失败
	 */
	public static long useLimitCount(RedisHelper redisHelper, RedisLimitParam limitParam, String key, int count) {
		if(!checkParam(limitParam, key)) {
			return -1;
		}
		
		final String fkey = getKey(limitParam, key);
		
		return (long) redisHelper.execute(jedis -> {
			try {
				Long retVal = null;
				if(count == 1) {
					retVal = jedis.incr(fkey);
				} else {
					retVal = jedis.incrBy(fkey, count);
				}
				
				if(retVal == null) {
					LOGGER.error("useLimitCount fail,namespace:{},key:{},count:{},ret is null",
							limitParam.getNamespace(), fkey, count);
					return -1L;
				}
				
				if(retVal == count && limitParam.getLimitPeroid().getExpireSecond() >= 0) {
					jedis.expire(fkey, limitParam.getLimitPeroid().getExpireSecond());
				}
				
				if(retVal <= limitParam.getLimitCount()) {
					return retVal;
				} else {
					if(count == 1) { // 还原现场
						jedis.decr(fkey);
					} else {
						jedis.decrBy(fkey, count);
					}
					return -1L; // 已经超额
				}
			} catch (Exception e) {
				LOGGER.error("getLimitCount error, namespace:{}, key:{}",
						limitParam.getNamespace(), fkey, e);
				return -1L;
			}
		});
	}
	
	/**
	 * 使用了一次限制。一般来说，业务都是在处理成功后才扣减使用是否成功的限制，
	 * 如果使用失败了，如果业务支持事务回滚，那么可以回滚掉，此时可以不用RedisTransation做全局限制。
	 * 
	 * @param redisHelper
	 * @param limitEnum
	 * @param key
	 * @return 返回是当前周期内第几个使用配额的，如果返回-1，表示使用配额失败
	 */
	public static long useLimitCount(RedisHelper redisHelper, RedisLimitParam limitEnum, String key) {
		return useLimitCount(redisHelper, limitEnum, key, 1);
	}
	
	private static String getKey(RedisLimitParam limitParam, String key) {
		String time = null;
		Date now = new Date();
		if(limitParam.getLimitPeroid() == RedisLimitPeroidEnum.SECOND) {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			time = df.format(now);
		} else if (limitParam.getLimitPeroid() == RedisLimitPeroidEnum.TEN_SECOND) {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			time = df.format(now);
			time = time.substring(0, time.length() - 1);
		} else if (limitParam.getLimitPeroid() == RedisLimitPeroidEnum.MINUTE) {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
			time = df.format(now);
		} else if (limitParam.getLimitPeroid() == RedisLimitPeroidEnum.HOUR) {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHH");
			time = df.format(now);
		} else if (limitParam.getLimitPeroid() == RedisLimitPeroidEnum.DAY) {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
			time = df.format(now);
		} else if (limitParam.getLimitPeroid() == RedisLimitPeroidEnum.WEEK_START_SUNDAY) {
			Calendar c = Calendar.getInstance();
			c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			if(c.getTime().after(new Date())) {
				c.add(Calendar.DATE, -7);
			}
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
			time = df.format(c.getTime());
		} else if (limitParam.getLimitPeroid() == RedisLimitPeroidEnum.WEEK_START_MONDAY) {
			Calendar c = Calendar.getInstance();
			c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			if(c.getTime().after(new Date())) {
				c.add(Calendar.DATE, -7);
			}
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
			time = df.format(c.getTime());
		} else if (limitParam.getLimitPeroid() == RedisLimitPeroidEnum.MONTH) {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMM");
			time = df.format(now);
		} else if (limitParam.getLimitPeroid() == RedisLimitPeroidEnum.YEAR) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy");
			time = df.format(now);
		} else if (limitParam.getLimitPeroid() == RedisLimitPeroidEnum.PERMANENT) {
			time = "";
		}
		
		return limitParam.getNamespace() + ":" + key + "-" + time;
	}
	
	private static boolean checkParam(RedisLimitParam limitParam, String key) {
		if(limitParam == null || key == null) {
			LOGGER.error("limitEnum or key is null, limitEnum:{}, key:{}", limitParam, key);
			return false;
		}
		
		if(limitParam.getNamespace() == null || limitParam.getNamespace().trim().isEmpty()) {
			LOGGER.error("limitEnum.namespace is blank");
			return false;
		}
		if(limitParam.getLimitPeroid() == null) {
			LOGGER.error("limitEnum.limitPeroid is null");
			return false;
		}
		if(limitParam.getLimitCount() <= 0) {
			LOGGER.error("limitEnum.limitCount must bigger than zero");
			return false;
		}
		
		return true;
	}
}
