package com.pugwoo.wooutils.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * 2016年5月28日 11:10:20
 * @author pugwoo
 *
 * Jedis实例不是线程安全的，所以new Jedis()只能给当前线程用。
 * 但是不断地new Jedis()是不合适的，推荐的做法就是用JedisPool。
 */
public class RedisConnectionManager {
	
	private static String host = "127.0.0.1";
	private static String port = "6379";
	private static String passwd = null;

	/**
	 * 单例的JedisPool，实际项目中可以配置在string，也可以是懒加载
	 */
	private static final JedisPool pool;
	
	static {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(128); // 最大链接数
		poolConfig.setMaxIdle(64);
		poolConfig.setMaxWaitMillis(1000L);
		poolConfig.setTestOnBorrow(false);
		poolConfig.setTestOnReturn(false);
		
		if(passwd == null || passwd.trim().isEmpty()) {
			// 0是connectionTimeout，还可以指定用0~15哪个redis库
			pool = new JedisPool(poolConfig, host, Integer.valueOf(port),
					Protocol.DEFAULT_TIMEOUT);
		} else {
			// 0是connectionTimeout，还可以指定用0~15哪个redis库
			pool = new JedisPool(poolConfig, host, Integer.valueOf(port),
					Protocol.DEFAULT_TIMEOUT, passwd);
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		if(pool != null && !pool.isClosed()) {
			pool.close();
		}
		super.finalize();
	}
	
	/**
	 * 拿Jedis连接，用完Jedis之后【必须】close jedis，这个非常重要
	 */
	public static Jedis getJedisConnection() {
		return pool.getResource();
	}
	
}
