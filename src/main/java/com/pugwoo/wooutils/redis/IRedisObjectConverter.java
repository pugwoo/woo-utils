package com.pugwoo.wooutils.redis;

/**
 * 对象转换接口
 * @author nick
 */
public interface IRedisObjectConverter {

	/**
	 * 将对象转换成字符串，【注意】需要自行处理null值的情况
	 *
	 * @param t 需要转换成json的对象
	 * @return
	 */
	<T> String convertToString(T t);
	
	/**
	 * 将字符串转换成对象，【注意】需要自行处理str为null值的情况
	 *
	 * @param str json字符串
	 * @param clazz 转换成的类
	 * @return
	 */
	<T> T convertToObject(String str, Class<T> clazz);

    /**
     * 将字符串转换成对象，支持一个泛型【注意】需要自行处理str为null值的情况
     * @param str json字符串
	 * @param clazz 转换成的类
	 * @param genericClass 泛型类
     * @return
     */
    <T> T convertToObject(String str, Class<T> clazz, Class<?> genericClass);

    /**
     * 将字符串转换成对象，，支持2个泛型【注意】需要自行处理str为null值的情况
     *
     * @param str json字符串
     * @param clazz 转换成的类
	 * @param genericClass1 泛型类1
	 * @param genericClass2 泛型类2
     * @return
     */
    <T> T convertToObject(String str, Class<T> clazz, Class<?> genericClass1, Class<?> genericClass2);
	
}
