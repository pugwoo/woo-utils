package com.pugwoo.wooutils.redis;

/**
 * 对象转换接口
 * @author nick
 */
public interface IRedisObjectConverter {

	/**
	 * 将对象转换成字符串，【注意】需要自行处理null值的情况
	 * @param t
	 * @return
	 */
	<T> String convertToString(T t);
	
	/**
	 * 将字符串转换成对象，【注意】需要自行处理str为null值的情况
	 * @param str
	 * @return
	 */
	<T> T convertToObject(String str, Class<T> clazz);

    /**
     * 将字符串转换成对象，【注意】需要自行处理str为null值的情况
     *
     * @param str
     * @return
     */
    <T> T convertToObject(String str, Class<T> clazz, Class<?> genericClass);

    /**
     * 将字符串转换成对象，【注意】需要自行处理str为null值的情况
     *
     * @param str
     * @param
     * @return
     */
    <T> T convertToObject(String str, Class<T> clazz, Class<?> genericClass1, Class<?> genericClass2);
	
}
