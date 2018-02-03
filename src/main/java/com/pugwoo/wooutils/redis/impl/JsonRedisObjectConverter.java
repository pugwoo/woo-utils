package com.pugwoo.wooutils.redis.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pugwoo.wooutils.json.NullKeySerializer;
import com.pugwoo.wooutils.redis.IRedisObjectConverter;

/**
 * 使用json只序列化field，不序列化getter setter
 */
public class JsonRedisObjectConverter implements IRedisObjectConverter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JsonRedisObjectConverter.class);
	
	private ObjectMapper mapper;
	{
		mapper  = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // 对于没有任何getter的bean序列化不抛异常
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); //属性不存在的兼容处理
		mapper.getSerializerProvider().setNullKeySerializer(new NullKeySerializer()); // 当map含有null key时，转成空字符串
	}
	
	@Override
	public <T> String convertToString(T t) {
		if(t == null) {
			return null;
		}
		try {
			return mapper.writeValueAsString(t);
		} catch (JsonProcessingException e) {
			LOGGER.error("convert object to json string fail", e);
			return null;
		}
	}

	@Override
	public <T> T convertToObject(String str, Class<T> clazz) {
		if(str == null || str.isEmpty()) {
			return null;
		}
		try {
			return mapper.readValue(str, clazz);
		} catch (Exception e) {
			LOGGER.error("convert json string to object fail", e);
			return null;
		}
	}

}
