package com.pugwoo.wooutils.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.pugwoo.wooutils.lang.DateUtils;
import com.pugwoo.wooutils.string.StringTools;

import java.io.IOException;
import java.time.LocalDate;

public class MultiLocalDateDeserializer extends StdDeserializer<LocalDate> {

	private static final long serialVersionUID = 1L;

	public MultiLocalDateDeserializer() {
		this(null);
	}
	public MultiLocalDateDeserializer(Class<?> vc) {
		super(vc);
	}
	
	@Override
	public LocalDate deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
		JsonNode node = jp.getCodec().readTree(jp);
		String date = node.asText();
		if (StringTools.isBlank(date)) { // 这个判空是因为parseLocalDate中没有判空
			return null;
		}

		LocalDate localDate = DateUtils.parseLocalDate(date);
		if (localDate == null) {
			throw new JsonParseException(jp,
						"Unparseable localDate: \"" + date + "\". Supported formats: "
								+ DateUtils.DATE_FORMAT_REGEXPS.values());
		}
		return localDate;
	}
	
}