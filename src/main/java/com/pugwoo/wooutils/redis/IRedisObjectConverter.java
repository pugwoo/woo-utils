package com.pugwoo.wooutils.redis;

/**
 * 对象转换接口
 * @author nick
 */
public interface IRedisObjectConverter {

	/**
	 * 将对象转换成字符串
	 * @param t
	 * @return
	 */
	<T> String convertToString(T t);
	
	/**
	 * 将字符串转换成对象
	 * @param str
	 * @return
	 */
	<T> T convertToObject(String str, Class<T> clazz);
	
}
