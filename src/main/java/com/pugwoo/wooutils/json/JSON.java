package com.pugwoo.wooutils.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;

/**
 * 封装起来的常用的json方法
 * @author NICK
 */
public class JSON {

	private static ObjectMapper objectMapper = new MyObjectMapper();

	/**只用于克隆对象用*/
	private static final ObjectMapper OBJECT_MAPPER_FOR_CLONE = new ObjectMapper();

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

	/**
	 * 解析json，只支持一层的泛型，不支持嵌套的泛型。
	 * 说明：实际上用JavaType也是可以做到支持嵌套的，但是比较复杂，在表达上可读性不高，这种场景也不多，故先不封装。
	 */
	public static <T> T parse(String str, Class<T> clazz, Class<?>... genericClasses) {
		try {
			JavaType type =  objectMapper.getTypeFactory()
					.constructParametricType(clazz, genericClasses);
			return objectMapper.readValue(str, type);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 解析json字符串 通过TypeReference静态指定泛型
	 * @param str json字符串
	 * @param typeReference 类型引用实例
	 * @return t
	 */
	public static <T> T parse(String str, TypeReference<T> typeReference) {
		try {
			return objectMapper.readValue(str, typeReference);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 解析字符串为Map
	 * @param str
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> parseToMap(String str) {
		return parse(str, Map.class);
	}
	
	/**
	 * 解析对象，可以通过jackson的ObjectNode读取各种类型值
	 * @param str
	 * @return
	 */
	public static ObjectNode parseObject(String str) {
		return parse(str, ObjectNode.class);
	}
	
	/**
	 * 解析数组
	 * @param str
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<JsonNode> parseArray(String str) {
		return parse(str, List.class, JsonNode.class);
	}

	/**
	 * 将对象转换成json字符串
	 * @param obj
	 * @return
	 */
	public static String toJson(Object obj) {
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 转换对象为map
	 * @param obj java bean对象，主要不要传入单个值如string Date等
	 * @return
	 */
	public static Map<String, Object> toMap(Object obj) {
		return parseToMap(toJson(obj));
	}
	
	/**
	 * 使用json的方式克隆对象
	 * 【不支持泛型，请使用clone(T t, Class... genericClasses)或clone(T t, TypeReference typeReference) 以支持泛型】
	 * @param t
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T clone(T t) {
		if(t == null) {
			return null;
		}

		try {
			String json = OBJECT_MAPPER_FOR_CLONE.writeValueAsString(t);
			return (T) OBJECT_MAPPER_FOR_CLONE.readValue(json, t.getClass());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 使用json的方式克隆对象，支持泛型，支持多个泛型，
	 * 但【不支持】嵌套泛型，嵌套泛型请使用clone(T t, TypeReference typeReference)
	 */
	public static <T> T clone(T t, Class<?>... genericClasses) {
		if (t == null) {
			return null;
		}
		try {
			String json = OBJECT_MAPPER_FOR_CLONE.writeValueAsString(t);
			JavaType type =  OBJECT_MAPPER_FOR_CLONE.getTypeFactory()
					.constructParametricType(t.getClass(), genericClasses);
			return OBJECT_MAPPER_FOR_CLONE.readValue(json, type);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 使用json的方式克隆对象，通过TypeReference静态指定泛型
	 */
	public static <T> T clone(T t, TypeReference<T> typeReference) {
		if(t == null) {
			return null;
		}
		try {
			String json = OBJECT_MAPPER_FOR_CLONE.writeValueAsString(t);
			return OBJECT_MAPPER_FOR_CLONE.readValue(json, typeReference);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 允许拿到ObjectMapper进行修改
	 * @return
	 */
	public static ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	/**
	 * 允许重新设置objectMapper
	 * @param objectMapper
	 */
	public static void setObjectMapper(ObjectMapper objectMapper) {
		JSON.objectMapper = objectMapper;
	}
}
