package com.pugwoo.wooutils.json;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 封装起来的常用的json方法
 * @author NICK
 */
public class JSON {

	private static ObjectMapper objectMapper = new MyObjectMapper();
	
	public static Object parse(String str) {
		try {
			return objectMapper.readValue(str, Object.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T parse(String str, Class<T> clazz) {
		try {
			return objectMapper.readValue(str, clazz);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> parseObject(String str) {
		return parse(str, Map.class);
	}
	
	public static <T> T parse(String str, Class<T> clazz, Class<?> genericClass) {
		try {
			JavaType type = objectMapper.getTypeFactory().constructParametricType(
					clazz, genericClass);
			return objectMapper.readValue(str, type);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String toJson(Object obj) {
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

}
