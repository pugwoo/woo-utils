package com.pugwoo.wooutils.redis;

import com.pugwoo.wooutils.json.JSON;

public class MyRedisObjectConverter implements IRedisObjectConverter {

	@Override
	public <T> String convertToString(T t) {
		if(t == null) {
			return null;
		}
		return JSON.toJson(t);
	}

	@Override
	public <T> T convertToObject(String str, Class<T> clazz) {
		if(str == null) {
			return null;
		}
		return JSON.parse(str, clazz);
	}

}
