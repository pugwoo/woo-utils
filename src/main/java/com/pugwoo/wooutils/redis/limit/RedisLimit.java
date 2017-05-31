package com.pugwoo.wooutils.redis.limit;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pugwoo.wooutils.redis.RedisHelper;

/**
 * 使用redis控制全局的操作次数限制<br>
 * <br>
 * 【重要】注：RedisLimit并不保证全局的串行事务，因为redis和mysql的事务没有办法很好地合在一起，所以不搞太复杂。
 * 高并发的串行保证请使用RedisTransation <br>
 * <br>
 * 注：redis的incr或decr在redis删除某个expire key时，会出现多个线程拿到相同值的情况，
 * 所以incr或decr来做这个功能并不可靠。
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
		
		if(limitParam == null || key == null) {
			LOGGER.error("limitEnum or key is null, limitEnum:{}, key:{}", limitParam, key);
			return 0;
		}
		
		try {
			key = getKey(limitParam, key);
			String count = redisHelper.getString(key);
			if(count == null) {
				return limitParam.getLimitCount();
			} else {
				long limitCount = new Integer(count);
				if(limitCount > limitParam.getLimitCount()) {
					limitCount = limitParam.getLimitCount();
				}
				return limitCount;
			}
		} catch (Exception e) {
			LOGGER.error("getLimitCount error,namespace:{},key:{}", limitParam.getNamespace(), key, e);
			return -1;
		}
	}
	
	/**
	 * 判断是否还有限制次数。
	 * @param redisHelper
	 * @param limitParam
	 * @param key
	 * @return
	 */
	public static boolean hasLimitCount(RedisHelper redisHelper, RedisLimitParam limitParam, String key) {
		return getLimitCount(redisHelper, limitParam, key) > 0;
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
	
	/**
	 * 使用了count次限制。一般来说，业务都是在处理成功后才扣减使用是否成功的限制，
	 * 如果使用失败了，如果业务支持事务回滚，那么可以回滚掉，此时可以不用RedisTransation做全局限制。
	 * 
	 * @param redisHelper
	 * @param limitParam
	 * @param key
	 * @param count 一次可以使用掉多个count
	 * @return 返回是当前周期内第几个使用配额的，如果返回-1，表示使用配额失败
	 */
	public static long useLimitCount(RedisHelper redisHelper, RedisLimitParam limitParam, String key, int count) {
		if(limitParam == null || key == null) {
			LOGGER.error("limitEnum or key is null, limitEnum:{}, key:{}", limitParam, key);
			return -1;
		}
		
		try {
			key = getKey(limitParam, key);
					
			for(int i = 0; i < 1000; i++) { // 重试次数，限制重试次数上限
				String oldValue = redisHelper.getString(key);
				long newValue = 0;
				if(oldValue == null) {
					newValue = limitParam.getLimitCount() - count;
				} else {
					long old = new Integer(oldValue);
					if(old > limitParam.getLimitCount()) {
						old = limitParam.getLimitCount();
					}
					newValue = old - count;
				}
				if(newValue < 0) { // 已经没有使用限额
					return -1;
				}
				
				long expireSeconds = redisHelper.getExpireSecond(key);
				long restSeconds = getRestSeconds(limitParam.getLimitPeroid());
				Integer setRest = null;
				// 为了避免跨周期设置问题，只能将ttl的值变小，不能变大； -1和-2（key不存在）时可以设置
				if(expireSeconds < 0 || expireSeconds >= 0 && restSeconds <= expireSeconds) {
					setRest = (int) restSeconds;
				}
				
				if(redisHelper.compareAndSet(key, newValue + "", oldValue, setRest)) {
					return limitParam.getLimitCount() - newValue;
				} else {
					continue;
				}
			}

			LOGGER.error("useLimitCount after 1000 times try fail, namespace:{}, key:{}",
					limitParam.getNamespace(), key);
			return -1; // 最终失败
		} catch (Exception e) {
			LOGGER.error("getLimitCount error, namespace:{}, key:{}",
					limitParam.getNamespace(), key, e);
			return -1;
		}
	}
	
	private static String getKey(RedisLimitParam limitParam, String key) {
		return limitParam.getNamespace() + "-" + key;
	}
		
	/**
	 * 获得到周期剩余的时间
	 * @param peroidEnum
	 * @return 默认就是永久 -1
	 */
	private static long getRestSeconds(RedisLimitPeroidEnum peroidEnum) {
		if(peroidEnum == null) {
			return -1;
		}
		if(peroidEnum == RedisLimitPeroidEnum.MINUTE) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, 1);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			return (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000;
		}
		if(peroidEnum == RedisLimitPeroidEnum.HOUR) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.HOUR_OF_DAY, 1);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			return (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000;
		}
		if(peroidEnum == RedisLimitPeroidEnum.DAY) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			return (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000;
		}
		if(peroidEnum == RedisLimitPeroidEnum.WEEK_START_SUNDAY) {
			return secondsToNextWeek(Calendar.SUNDAY);
		}
		if(peroidEnum == RedisLimitPeroidEnum.WEEK_START_MONDAY) {
			return secondsToNextWeek(Calendar.MONDAY);
		}
		if(peroidEnum == RedisLimitPeroidEnum.MONTH) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, 1);
			cal.set(Calendar.DATE, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			return (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000;
		}
		if(peroidEnum == RedisLimitPeroidEnum.YEAR) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.YEAR, 1);
			cal.set(Calendar.MONTH, 0);
			cal.set(Calendar.DATE, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			return (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000;
		}
		if(peroidEnum == RedisLimitPeroidEnum.PERMANENT) {
			return -1;
		}
		
		return -1;
	}
	
	/**
	 * 传入的是Calendar.SUNDAY  Calendar.MONDAY
	 * @param i
	 * @return
	 */
	private static long secondsToNextWeek(int i) {
		Calendar cal = Calendar.getInstance();
		do {
			cal.add(Calendar.DATE, 1);
		} while(cal.get(Calendar.DAY_OF_WEEK) != i);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000;
	}
	
}
