package com.pugwoo.wooutils.yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pugwoo.wooutils.string.StringTools;

public class YAML {

    /** 全局配置的yaml objectMapper */
    private static ObjectMapper objectMapper = new MyYamlObjectMapper();

    /**
     * 解析yaml字符串
     * @param yamlStr yaml字符串
     * @param clazz 对象类型
     * @return t
     */
    public static <T> T parse(String yamlStr, Class<T> clazz) {
        try {
            return objectMapper.readValue(yamlStr, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析yaml字符串，从自定的路径开始解析
     * @param yamlStr yaml字符串
     * @param path 路径，例如 /spring/redis 则可解析yaml下spring.redis下的局部yaml
     * @param clazz 对象类型
     * @return t
     */
    public static <T> T parse(String yamlStr, String path, Class<T> clazz) {
        if (StringTools.isBlank(path)) {
            return parse(yamlStr, clazz);
        }

        try {
            JsonNode node = objectMapper.readTree(yamlStr);
            JsonNode atPath = node.at(path);
            return objectMapper.treeToValue(atPath, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
