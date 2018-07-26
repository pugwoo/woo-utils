package com.pugwoo.wooutils.redis.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pugwoo.wooutils.collect.ListUtils;
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
	
	/**约定：当host为null或blank时，表示不初始化*/
	protected String host = null;
	
	private Integer port = 6379;
	private Integer maxConnection = 128;
	private String password = null;
	/**指定0~15哪个redis库*/
	private Integer database = 0;
	/**String和Object之间的转换对象*/
	private IRedisObjectConverter redisObjectConverter;
	
	/**
	 * 单例的JedisPool，懒加载初始化
	 */
	private JedisPool pool;
	
	@Override
	public Jedis getJedisConnection() {
		if(pool == null) {
			synchronized (this) {
				if(pool == null && host != null && !host.trim().isEmpty()) {
					JedisPoolConfig poolConfig = new JedisPoolConfig();
					poolConfig.setMaxTotal(maxConnection); // 最大链接数
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
		if(pool == null) {return null;}
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
		} catch (Exception e) {
			if(jedis != null) {
				jedis.close();
				jedis = null;
			}
		}
		return jedis;
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
		if(value == null) { // null值不需要设置
			return true;
		}
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
		if(value == null) { // null值不需要设置
			return true;
		}
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
	public List<String> getStrings(List<String> keys) {
		if(keys == null || keys.isEmpty()) {
			return new ArrayList<>();
		}
		Jedis jedis = null;
		try {
			jedis = getJedisConnection();
			List<String> strs = jedis.mget(keys.toArray(new String[0]));
			return strs;
		} catch (Exception e) {
			LOGGER.error("operate jedis error, keys:{}", keys, e);
			return null;
		} finally {
			if (jedis != null) {
				try {
					jedis.close();
				} catch (Exception e) {
					LOGGER.error("close jedis error, keys:{}", keys, e);
				}
			}
		}
	}
	
	@Override
	public <T> List<T> getObjects(List<String> keys, Class<T> clazz) {
		if(redisObjectConverter == null) {
			throw new RuntimeException("IRedisObjectConverter is null");
		}
		List<String> values = getStrings(keys);
		if(values == null) {
			return null;
		}
		return ListUtils.transform(values, 
				o -> redisObjectConverter.convertToObject(o, clazz));
	}
	
	@Override
	public Set<String> getKeys(String pattern) {
		Jedis jedis = null;
		try {
			jedis = getJedisConnection();
			return jedis.keys(pattern);
		} catch (Exception e) {
			LOGGER.error("operate jedis KEYS error, pattern:{}", pattern, e);
			return null;
		} finally {
			if (jedis != null) {
				try {
					jedis.close();
				} catch (Exception e) {
					LOGGER.error("close jedis error, pattern:{}", pattern, e);
				}
			}
		}
	}
	
	@Override
	public Map<String, String> getStrings(String pattern) {
		Set<String> keys = getKeys(pattern);
		if(keys == null) return null;
		if(keys.isEmpty()) {
			return new HashMap<>();
		}
		
		List<String> keyList = new ArrayList<>(keys);
		List<String> vals = getStrings(keyList);
		if(vals == null) return null;
		if(keyList.size() != vals.size()) {
			return null;
		}
		Map<String, String> map = new HashMap<>();
		for(int i = 0; i < keyList.size(); i++) {
			map.put(keyList.get(i), vals.get(i));
		}
		
		return map;
	}
	
	@Override
	public <T> Map<String, T> getObjects(String pattern, Class<T> clazz) {
		if(redisObjectConverter == null) {
			throw new RuntimeException("IRedisObjectConverter is null");
		}
		
		Map<String, String> vals = getStrings(pattern);
		if(vals == null) return null;
		
		Map<String, T> result = new HashMap<>();
		for(Entry<String, String> e : vals.entrySet()) {
			result.put(e.getKey(), redisObjectConverter.
					convertToObject(e.getValue(), clazz));
		}
		
		return result;
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
		if(value == null) { // 不支持value设置为null
			return false;
		}
		Jedis jedis = null;
		try {
			jedis = getJedisConnection();
			jedis.watch(key);
			String readOldValue = jedis.get(key);
			if(Objects.equals(readOldValue, oldValue)) {
				Transaction tx = jedis.multi();
				Response<String> result = null;
				if(expireSeconds != null && expireSeconds >= 0) {
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
		return RedisLimit.getLimitCount(this, limitParam, key) > 0;
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
		return RedisLock.requireLock(this, namespace, key, maxTransactionSeconds);
	}
	
	@Override
	public boolean releaseLock(String namespace, String key) {
		return RedisLock.releaseLock(this, namespace, key);
	}
	
	@Override
	public Long getAutoIncrementId(String namespace) {
		return RedisAutoIncrementId.getAutoIncrementId(this, namespace);
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
		if(port != null && port >= 0) {
			this.port = port;
		}
	}
	
	public Integer getMaxConnection() {
		return maxConnection;
	}

	public void setMaxConnection(Integer maxConnection) {
		this.maxConnection = maxConnection;
	}

	public Integer getDatabase() {
		return database;
	}

	public void setDatabase(Integer database) {
		if(database != null && database >= 0) {
			this.database = database;
		}
	}

	public IRedisObjectConverter getRedisObjectConverter() {
		return redisObjectConverter;
	}

	public void setRedisObjectConverter(IRedisObjectConverter redisObjectConverter) {
		this.redisObjectConverter = redisObjectConverter;
	}
	
}
