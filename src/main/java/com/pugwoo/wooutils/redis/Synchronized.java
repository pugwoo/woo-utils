package com.pugwoo.wooutils.redis;

/**
 * 分布式锁
 * @author nick markfly
 */
public @interface Synchronized {

	/**
	 * 分布式锁命名空间，每个业务有自己唯一的分布式锁namespace，相同的namespace表示同一把锁。
	 * @return
	 */
	String namespace();
	
}
