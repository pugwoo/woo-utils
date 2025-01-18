package com.pugwoo.wooutils.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.pugwoo.wooutils.lang.DateUtils;
import com.pugwoo.wooutils.string.StringTools;

import java.io.IOException;
import java.time.LocalTime;

public class MultiLocalTimeDeserializer extends StdDeserializer<LocalTime> {

	private static final long serialVersionUID = 1L;

	public MultiLocalTimeDeserializer() {
		this(null);
	}
	public MultiLocalTimeDeserializer(Class<?> vc) {
		super(vc);
	}
	
	@Override
	public LocalTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
		JsonNode node = jp.getCodec().readTree(jp);
		String date = node.asText();
		if (StringTools.isBlank(date)) { // 这个判空是因为parseLocalDate中没有判空
			return null;
		}

		LocalTime localTime = DateUtils.parseLocalTime(date);
		if (localTime == null) {
			throw new JsonParseException(jp,
						"Unparseable localTime: \"" + date + "\". Supported formats: "
								+ DateUtils.DATE_FORMAT_REGEXPS.values());
		}
		return localTime;
	}
	
}