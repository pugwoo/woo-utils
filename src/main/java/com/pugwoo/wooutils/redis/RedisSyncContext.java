package com.pugwoo.wooutils.redis;

/**
 * 记录最后一次@Synchronized的执行结果信息
 * @author nick
 */
public class RedisSyncContext {
	
	private static ThreadLocal<RedisSyncContext> contextTL = new ThreadLocal<RedisSyncContext>();
	
	// 是否是串行执行，当redisHelper没有提供时为false。一般不需要来检查这个值。
	private boolean isSync;
	
	// 是否有执行方法
	private boolean haveRun;
		
	/**
	 * 设置最后一次执行的结果信息。必须一次性设置完
	 */
	protected static void set( boolean isSync, boolean haveRun) {
		RedisSyncContext context = new RedisSyncContext();
		context.isSync = isSync;
		context.haveRun = haveRun;
		
		contextTL.set(context);
	}

	/**
	 * 是否有执行了方法
	 * @return
	 */
	public static boolean getHaveRun() {
		RedisSyncContext context = contextTL.get();
		if(context == null) {
			return false;
		}
		return context.haveRun;
	}

	/**
	 * 是否是串行执行，当redisHelper没有提供时为false。一般不需要来检查这个值。
	 * @return
	 */
	public static boolean getIsSync() {
		RedisSyncContext context = contextTL.get();
		if(context == null) {
			return false;
		}
		return context.isSync;
	}
	
}
