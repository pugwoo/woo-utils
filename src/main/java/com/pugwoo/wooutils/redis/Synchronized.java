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
	
	/**
	 * 事务超时的秒数，如果使用者超过这个时间还没有主动释放锁，那么redis会自动释放掉该锁。
	 * 请使用者合理评估任务执行时间，推荐按正常执行时间的10倍~100倍评估该时间。
	 * @return
	 */
	int expireSecond() default 60;
	
	
}
