package com.pugwoo.wooutils.yaml;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class MyYamlObjectMapper extends ObjectMapper {

    private static final long serialVersionUID = 7802045661502663727L;

    public MyYamlObjectMapper() {
        super(new YAMLFactory());

        // 允许出现未知的值，适合于向下兼容
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }

}
