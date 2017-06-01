package com.pugwoo.wooutils.redis;

import com.alibaba.fastjson.JSON;

public class MyRedisObjectConverter implements IRedisObjectConverter {

	@Override
	public <T> String convertToString(T t) {
		if(t == null) {
			return null;
		}
		return JSON.toJSONString(t);
	}

	@Override
	public <T> T convertToObject(String str, Class<T> clazz) {
		if(str == null) {
			return null;
		}
		return JSON.parseObject(str, clazz);
	}

}
