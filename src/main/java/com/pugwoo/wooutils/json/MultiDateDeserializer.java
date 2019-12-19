package com.pugwoo.wooutils.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.pugwoo.wooutils.lang.DateUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

public class MultiDateDeserializer extends StdDeserializer<Date> {
	
	private static final long serialVersionUID = 1L;

	public MultiDateDeserializer() {
		this(null);
	}
	public MultiDateDeserializer(Class<?> vc) {
		super(vc);
	}
	
	@Override
	public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);

		// 针对时间戳做优化
		if(node instanceof LongNode || node instanceof IntNode) {
			long timestamp = node.asLong();
			if(timestamp < 4200000000L) { // 小于42亿认为是秒
				return new Date(timestamp * 1000L);
			} else {
				return new Date(timestamp);
			}
		}

		String date = node.asText();
		if(date == null) {
			return null;
		}
		date = date.trim();
		if(date.isEmpty()) {
			return null;
		}
		
		try {
			return DateUtils.parseThrowException(date);
		} catch (ParseException e) {
			throw new JsonParseException(jp,
			    "Unparseable date: \"" + date + "\". Supported formats: " 
			    + DateUtils.DATE_FORMAT_REGEXPS.values());
		}
	}
	
}