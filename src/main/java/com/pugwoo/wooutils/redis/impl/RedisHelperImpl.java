package com.pugwoo.wooutils.redis.impl;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pugwoo.wooutils.redis.IRedisObjectConverter;
import com.pugwoo.wooutils.redis.RedisHelper;
import com.pugwoo.wooutils.redis.RedisLimitParam;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

/**
 * 大部分实现时间: 2016年11月2日 15:10:21
 * @author nick
 */
public class RedisHelperImpl implements RedisHelper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisHelperImpl.class);
	
	private String host = "127.0.0.1";
	private Integer port = 6379;
	private String password = null;
	/**指定0~15哪个redis库*/
	private Integer database = 0;
	/**String和Object之间的转换对象*/
	private IRedisObjectConverter redisObjectConverter;
	
	/**
	 * 单例的JedisPool，实际项目中可以配置在string，也可以是懒加载
	 */
	private JedisPool pool;
	
	@Override
	public Jedis getJedisConnection() {
		if(pool == null) {
			synchronized (this) {
				if(pool == null) {
					JedisPoolConfig poolConfig = new JedisPoolConfig();
					poolConfig.setMaxTotal(128); // 最大链接数
					poolConfig.setMaxIdle(64);
					poolConfig.setMaxWaitMillis(1000L);
					poolConfig.setTestOnBorrow(false);
					poolConfig.setTestOnReturn(false);
					
					if(password != null && password.trim().isEmpty()) {
						password = null;
					}
					
					pool = new JedisPool(poolConfig, host, Integer.valueOf(port),
							Protocol.DEFAULT_TIMEOUT, password, database);
				}
			}
		}
		return pool.getResource();
	}
	
	@Override
	protected void finalize() throws Throwable {
		if(pool != null && !pool.isClosed()) {
			pool.close();
		}
		super.finalize();
	}
	
	@Override
	public boolean setString(String key, int expireSecond, String value) {
		Jedis jedis = null;
		try {
			jedis = getJedisConnection();
			jedis.setex(key, expireSecond, value);
			return true;
		} catch (Exception e) {
			LOGGER.error("operate jedis error, key:{}, value:{}", key, value, e);
			return false;
		} finally {
			if (jedis != null) {
				try {
					jedis.close();
				} catch (Exception e) {
					LOGGER.error("close jedis error, key:{}, value:{}", key, value, e);
				}
			}
		}
	}
	
	@Override
	public <T> boolean setObject(String key, int expireSecond, T value) {
		if(redisObjectConverter == null) {
			throw new RuntimeException("IRedisObjectConverter is null");
		}
		String v = redisObjectConverter.convertToString(value);
		return setString(key, expireSecond, v);
	}
	
	@Override
	public boolean setStringIfNotExist(String key, int expireSecond, String value) {
		Jedis jedis = null;
		try {
			jedis = getJedisConnection();
			String result = jedis.set(key, value, "NX", "EX", expireSecond);
			return result != null;
		} catch (Exception e) {
			LOGGER.error("operate jedis error, key:{}, value:{}", key, value, e);
			return false;
		} finally {
			if (jedis != null) {
				try {
					jedis.close();
				} catch (Exception e) {
					LOGGER.error("close jedis error, key:{}, value:{}", key, value, e);
				}
			}
		}
	}
	
	@Override
	public boolean setExpire(String key, int expireSecond) {
		Jedis jedis = null;
		try {
			jedis = getJedisConnection();
			jedis.expire(key, expireSecond);
			return true;
		} catch (Exception e) {
			LOGGER.error("operate jedis error, key:{}", key, e);
			return false;
		} finally {
			if (jedis != null) {
				try {
					jedis.close();
				} catch (Exception e) {
					LOGGER.error("close jedis error, key:{}", key, e);
				}
			}
		}
	}
	
	@Override
	public long getExpireSecond(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedisConnection();
			return jedis.ttl(key);
		} catch (Exception e) {
			LOGGER.error("operate jedis error, key:{}", key, e);
			return -999;
		} finally {
			if (jedis != null) {
				try {
					jedis.close();
				} catch (Exception e) {
					LOGGER.error("close jedis error, key:{}", key, e);
				}
			}
		}
	}
	
	@Override
	public String getString(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedisConnection();
			String str = jedis.get(key);
			return str;
		} catch (Exception e) {
			LOGGER.error("operate jedis error, key:{}", key, e);
			return null;
		} finally {
			if (jedis != null) {
				try {
					jedis.close();
				} catch (Exception e) {
					LOGGER.error("close jedis error, key:{}", key, e);
				}
			}
		}
	}
	
	@Override
	public <T> T getObject(String key, Class<T> clazz) {
		if(redisObjectConverter == null) {
			throw new RuntimeException("IRedisObjectConverter is null");
		}
		String value = getString(key);
		if(value == null) {
			return null;
		}
		
		return redisObjectConverter.convertToObject(value, clazz);
	}
	
	@Override
	public boolean remove(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedisConnection();
			jedis.del(key);
			return true;
		} catch (Exception e) {
			LOGGER.error("operate jedis error, key:{}", key, e);
			return false;
		} finally {
			if (jedis != null) {
				try {
					jedis.close();
				} catch (Exception e) {
					LOGGER.error("close jedis error, key:{}", key, e);
				}
			}
		}
	}
	
	@Override
	public boolean compareAndSet(String key, String value, String oldValue, Integer expireSeconds) {
		Jedis jedis = null;
		try {
			jedis = getJedisConnection();
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
		} finally {
			if (jedis != null) {
				try {
					jedis.close();
				} catch (Exception e) {
					LOGGER.error("close jedis error, key:{}", key, e);
				}
			}
		}
	}

	@Override
	public long getLimitCount(RedisLimitParam limitParam, String key) {
		return RedisLimit.getLimitCount(this, limitParam, key);
	}

	@Override
	public boolean hasLimitCount(RedisLimitParam limitParam, String key) {
		return RedisLimit.hasLimitCount(this, limitParam, key);
	}

	@Override
	public long useLimitCount(RedisLimitParam limitEnum, String key) {
		return RedisLimit.useLimitCount(this, limitEnum, key);
	}

	@Override
	public long useLimitCount(RedisLimitParam limitParam, String key, int count) {
		return RedisLimit.useLimitCount(this, limitParam, key, count);
	}
	
	@Override
	public boolean requireLock(String namespace, String key, int maxTransactionSeconds) {
		return RedisTransaction.requireLock(this, namespace, key, maxTransactionSeconds);
	}
	
	@Override
	public boolean releaseLock(String namespace, String key) {
		return RedisTransaction.releaseLock(this, namespace, key);
	}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Integer getDatabase() {
		return database;
	}

	public void setDatabase(Integer database) {
		this.database = database;
	}

	public IRedisObjectConverter getRedisObjectConverter() {
		return redisObjectConverter;
	}

	public void setRedisObjectConverter(IRedisObjectConverter redisObjectConverter) {
		this.redisObjectConverter = redisObjectConverter;
	}
	
}
