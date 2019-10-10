package com.pugwoo.wooutils.redis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分布式锁，注解在方法上。 暂时不支持还按照方法的参数来独立限制。
 * 
 * 关于返回值：如果方法是常规返回值，当没有获取到锁时，且超过了阻塞等待时间，则返回null。
 * 如果想拿到更多的信息，可以通过RedisSyncContext拿，线程独立。
 *
 * 【关于锁的可重入性】@Synchronized本身是不支持锁的可重入性，原因见RedisLock类说明。但是由于AOP的特性，自身方法的递归是不涉及到AOP的，也即天然满足了锁可重入特性，所以@Synchronized是支持自身方法递归的。但是对于类之间的循环调用，则应避免。
 *
 * @author nick markfly
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Synchronized {

	/**
	 * 分布式锁命名空间，每个业务有自己唯一的分布式锁namespace，相同的namespace表示同一把锁。
	 * @return
	 */
	String namespace();

	/**
	 * [可选] 分布式锁的不同的key的mvel表达式脚本，可以从参数列表变量args中获取
	 * @return 【重要】如果脚本执行出错，则打log，并等价于空字符串，并不会抛出异常阻止调用进行
	 */
	String keyScript() default "";
	
	/**
	 * 事务超时的秒数，如果使用者超过这个时间还没有主动释放锁，那么redis会自动释放掉该锁。
	 * 请使用者合理评估任务执行时间，推荐按正常执行时间的10倍~100倍评估该时间。
	 * @return
	 */
	int expireSecond() default 60;
	
	/**
	 * 当进程/线程没有拿到锁时，阻塞等待的时间，单位毫秒，默认10000毫秒
	 * （取10秒这个值考虑的是人类等待计算机反馈的不耐烦的大概时间）。
	 * 如果不需要阻塞，请设置为0.
	 * @return
	 */
	int waitLockMillisecond() default 10000;

	/**
	 * 是否开启记录获得锁和释放锁的日志，默认关闭
	 * @return
	 */
	boolean logDebug() default false;
	
}
