package com.pugwoo.wooutils.json;

import java.text.SimpleDateFormat;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MyObjectMapper extends ObjectMapper {

	private static final long serialVersionUID = 7802045661502663726L;

	public MyObjectMapper() {
		super();
		
		// 个性化配置
		setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")); // 设置日期格式
		configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); //属性不存在的兼容处理
		
		configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true); //属性key可以不用括号
		configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true); //属性key使用单引号
		configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true); //允许数字以0开头
		configure(JsonParser.Feature.ALLOW_MISSING_VALUES, true); //允许[]中有多个,,,
		configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true); //允许[]最后带多一个,
		
	}
	
}
