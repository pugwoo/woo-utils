package com.pugwoo.wooutils.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pugwoo.wooutils.string.StringTools;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 封装起来的常用的json方法
 * @author NICK
 */
public class JSON {

	/** 全局配置的objectMapper */
	private static ObjectMapper objectMapper = new MyObjectMapper();

	/** 只用于克隆对象用，因此不需要过多的额外的配置 */
	private static final ObjectMapper OBJECT_MAPPER_FOR_CLONE = new ObjectMapper() {{
		configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); //属性不存在的兼容处理
		registerModule(new JavaTimeModule());
	}};
	
	/** 用于支持自定义ObjectMapper进行json操作 */
	private static final ThreadLocal<ObjectMapper> OBJECT_MAPPER_THREAD_LOCAL = new ThreadLocal<>();
	
	/** 类型引用 Map {@literal <} String, Object {@literal >} */
	public final static TypeReference<Map<String, Object>> TYPE_REFERENCE_MAP =
			new TypeReference<Map<String, Object>>() {};
	
	/** 类型引用 List{@literal <} Map {@literal <} String, Object {@literal >} {@literal >} */
	public final static TypeReference<List<Map<String, Object>>> TYPE_REFERENCE_LIST_MAP =
			new TypeReference<List<Map<String, Object>>>() {};
	
	/**
	 * 允许拿到ObjectMapper进行修改 <br>
	 *     用于修改配置，建议全项目在初始化阶段配置一次 <br>
	 *     如有不同的配置需求，也可使用一下方法进行json操作 <br>
	 *     1. {@link #setThreadObjectMapper(ObjectMapper)} + {@link #removeThreadObjectMapper()} <br>
	 *     2. {@link #useThreadObjectMapper(ObjectMapper, Procedure)} <br>
	 *     3. {@link #useThreadObjectMapper(ObjectMapper, Supplier)} <br>
	 *
	 * @return objectMapper
	 */
	public static ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	/**
	 * 允许重新设置objectMapper <br>
	 *     建议全项目在初始化阶段配置一次<br>
	 *
	 * @param objectMapper objectMapper
	 */
	public static void setGlobalObjectMapper(ObjectMapper objectMapper) {
		if (objectMapper == null) {
			throw new IllegalArgumentException("objectMapper must not be null!");
		}
		JSON.objectMapper = objectMapper;
	}
	
	/**
	 * 自定义objectMapper进行JSON操作 使用ThreadLocal实现 <br>
	 * 注意: 使用后请调用{@link #removeThreadObjectMapper()}清除<br>
	 * <br>
	 * 使用示例: <br>
	 * <pre>
try {
    JSON.setThreadObjectMapper(customObjectMapper)
    JSON.toJson(obj);
} finally {
    JSON.removeThreadObjectMapper();
}
	 * </pre>
	 *
	 * @param objectMapper objectMapper 如果为null将使用全局的ObjectMapper进行操作
	 */
	public static void setThreadObjectMapper(ObjectMapper objectMapper) {
		OBJECT_MAPPER_THREAD_LOCAL.set(objectMapper);
	}
	
	/**
	 * 清除线程的ObjectMapper
	 */
	public static void removeThreadObjectMapper() {
		OBJECT_MAPPER_THREAD_LOCAL.remove();
	}
	
	/**
	 * 使用自定义的ObjectMapper进行执行JSON操作 <br>
	 * <br>
	 * 使用示例:<br>
	 * <pre>
JSON.useThreadObjectMapper(customObjectMapper, () -{@literal >} {
    JSON.toJson(obj);
})
	 * </pre>
	 * @param objectMapper objectMapper 如果为null将使用全局的ObjectMapper进行操作
	 * @param procedure 无参无返回执行
	 */
	public static void useThreadObjectMapper(ObjectMapper objectMapper, Procedure procedure) {
		useThreadObjectMapper(objectMapper, () -> {
			procedure.run();
			return null;
		});
	}
	
	/**
	 * 使用自定义的ObjectMapper进行执行JSON操作 <br>
	 * <br>
	 * 使用示例:<br>
	 * <pre>
String json = JSON.useThreadObjectMapper(customObjectMapper, () -{@literal >} {
    return JSON.toJson(obj);
})
	 * </pre>
	 * @param objectMapper objectMapper 如果为null将使用全局的ObjectMapper进行操作
	 * @param supplier 无参有返回执行
	 */
	public static <T> T useThreadObjectMapper(ObjectMapper objectMapper, Supplier<T> supplier) {
		try {
			setThreadObjectMapper(objectMapper);
			return supplier.get();
		} finally {
			JSON.removeThreadObjectMapper();
		}
	}
	
	// -------------------------------------------------------------- 反序列化 json -> object
	
	/**
	 * 解析json
	 * @param str json字符串
	 * @return object
	 */
	public static Object parse(String str) {
		return parse(str, Object.class);
	}
	
	/**
	 * 解析json
	 * @param str json字符串
	 * @param clazz 对象类型
	 * @return t
	 */
	public static <T> T parse(String str, Class<T> clazz) {
		if (StringTools.isBlank(str)) {
			return null;
		}
		return parse(om -> om.readValue(str,clazz));
	}
	
	/**
	 * 解析json，只支持一层的泛型，不支持嵌套的泛型。
	 * 说明：实际上用JavaType也是可以做到支持嵌套的，但是比较复杂，在表达上可读性不高，这种场景也不多，故先不封装。
	 *
	 * @param str json字符串
	 * @param clazz 对象类型
	 * @param genericClasses 对象的泛型
	 * @return t
	 * @deprecated use {@link #parse(String str, TypeReference<T>)} instead
	 */
	@Deprecated
	public static <T> T parse(String str, Class<T> clazz, Class<?>... genericClasses) {
		if (StringTools.isBlank(str)) {
			return null;
		}
		return parse(om -> {
			JavaType type =  om.getTypeFactory().constructParametricType(clazz, genericClasses);
			return om.readValue(str, type);
		});
	}
	
	/**
	 * 解析json字符串 通过TypeReference静态指定泛型
	 * @param str json字符串
	 * @param typeReference 类型引用实例
	 * @return t
	 */
	public static <T> T parse(String str, TypeReference<T> typeReference) {
		if (StringTools.isBlank(str)) {
			return null;
		}
		return parse(om -> om.readValue(str, typeReference));
	}
	
	/**
	 * 解析字符串为Map
	 * @param str json字符串
	 * @return map
	 */
	public static Map<String, Object> parseToMap(String str) {
		return parse(str, TYPE_REFERENCE_MAP);
	}
	
	/**
	 * 解析字符串为ListMap
	 * @param str json字符串
	 * @return mapList
	 */
	public static List<Map<String, Object>> parseToListMap(String str) {
		return parse(str, TYPE_REFERENCE_LIST_MAP);
	}
	
	/**
	 * 解析字符串为Map
	 * @param str json字符串
	 * @return map
	 */
	public static <T> List<T> parseToList(String str, Class<T> itemClazz) {
		if (StringTools.isBlank(str)) {
			return null;
		}
		return parseToList(str, typeFactory ->
				typeFactory.constructParametricType(List.class, itemClazz)
		);
	}
	
	/**
	 * 将json字符串转换为 List {@literal <} T {@literal >}
	 *
	 * @param str json字符串
	 * @param itemTypeRef item的类型
	 * @return List {@literal <} T {@literal >}
	 */
	public static <T> List<T> parseToList(String str, TypeReference<T> itemTypeRef) {
		if (StringTools.isBlank(str)) {
			return null;
		}
		return parseToList(str, typeFactory -> {
			JavaType itemType = typeFactory.constructType(itemTypeRef);
			return typeFactory.constructParametricType(List.class, itemType);
		});
	}
	
	/**
	 * 解析对象，可以通过jackson的ObjectNode读取各种类型值
	 * @param str json字符串
	 * @return objectNode
	 */
	public static ObjectNode parseObject(String str) {
		return parse(str, ObjectNode.class);
	}
	
	/**
	 * 解析数组
	 * @param str json字符串
	 * @return jsonNodeList
	 */
	public static List<JsonNode> parseArray(String str) {
		return parseToList(str, JsonNode.class);
	}
	
	// -------------------------------------------------------------- 序列化 object -> json
	
	/**
	 * 将对象转换成json字符串
	 * @param obj obj
	 * @return json
	 */
	public static String toJson(Object obj) {
		return toJson(om -> om.writeValueAsString(obj));
	}
	
	/**
	 * 将对象转换为json字符串 格式化的
	 * @param obj obj
	 * @return json
	 */
	public static String toJsonFormatted(Object obj) {
		return toJson(om -> om.writerWithDefaultPrettyPrinter().writeValueAsString(obj));
	}
	
	// -------------------------------------------------------------- others
	
	/**
	 * 转换对象为map
	 * @param obj java bean对象，主要不要传入单个值如string Date等
	 * @return map
	 */
	public static Map<String, Object> toMap(Object obj) {
		return parseToMap(toJson(obj));
	}
	
	// -------------------------------------------------------------- clone
	
	/**
	 * 使用json的方式克隆对象
	 * 【不支持泛型，请使用{@link #clone(Object, Class[])}或{@link #clone(Object, TypeReference)}以支持泛型】
	 * @param t 被克隆对象
	 * @return 对象
	 */
	public static <T> T clone(T t) {
		return clone(t, typeFactory -> typeFactory.constructType(getClassFromT(t)));
	}
	
	/**
	 * 使用json的方式克隆对象，支持泛型，支持多个泛型，
	 * 但【不支持】嵌套泛型，嵌套泛型请使用{@link #clone(Object, TypeReference)}
	 * @param t 被克隆对象
	 * @param genericClasses 被克隆对象的泛型
	 * @return 对象
	 * @deprecated use {@link #clone(Object, TypeReference)} instead
	 */
	@Deprecated
	public static <T> T clone(T t, Class<?>... genericClasses) {
		return clone(t, typeFactory -> typeFactory.constructParametricType(getClassFromT(t), genericClasses));
	}
	
	/**
	 * 使用json的方式克隆对象，通过TypeReference静态指定泛型
	 */
	public static <T> T clone(T t, TypeReference<T> typeReference) {
		return clone(t, typeFactory -> typeFactory.constructType(typeReference));
	}

	private static <T> Class<?> getClassFromT(T t) {
		if (t == null) {
			return null;
		}
        Class<?> clazz = t.getClass();
		// 特别处理一些类型
		if ("java.util.ArrayList$SubList".equals(clazz.getName())) { // 这个SubList jackson处理不了
			return List.class;
		}
		return clazz;
	}
	
	// -------------------------------------------------------------- private
	
	/**
	 * 将json字符串转换为对象 T
	 *   适用于非常用的readValue外的其他方法调用，统一抛出{@link RuntimeException}
	 *   也可以自己通过{@link #getObjectMapper()}获取objectMapper自行操作
	 *
	 * @param function objectMapper 转成 T
	 * @return T
	 */
	private static <T> T parse(ObjectMapperFunc<T> function) {
		return execute("json deserialization failed", function);
	}
	
	/**
	 * 将对象转换为json字符串 只是捕获了异常统一抛出{@link RuntimeException}
	 *
	 * @param function objectMapper 转成 T
	 * @return json
	 */
	private static String toJson(ObjectMapperFunc<String> function) {
		return execute("json serialization failed", function);
	}
	
	/**
	 * 将json字符串转换为 List {@literal <} T {@literal >}
	 *
	 * @param str json字符串
	 * @param typeFactoryJavaTypeFunction typeFactory 转成 javaType
	 * @return List {@literal <} T {@literal >}
	 */
	private static <T> List<T> parseToList(String str, Function<TypeFactory, JavaType> typeFactoryJavaTypeFunction) {
		return parse(om -> {
			TypeFactory typeFactory = om.getTypeFactory();
			JavaType javaType = typeFactoryJavaTypeFunction.apply(typeFactory);
			return om.readValue(str, javaType);
		});
	}
	
	/**
	 * 使用json的方式克隆对象，通过javaType指定泛型
	 */
	private static <T> T clone(T t, Function<TypeFactory, JavaType> typeFactoryJavaTypeFunction) {
		if(t == null) { return null; }
		return execute(OBJECT_MAPPER_FOR_CLONE, "clone failed", om -> {
			TypeFactory typeFactory = om.getTypeFactory();
			JavaType javaType = typeFactoryJavaTypeFunction.apply(typeFactory);
			String json = om.writeValueAsString(t);
			return om.readValue(json, javaType);
		});
	}
	
	/**
	 * json处理 统一抛出{@link RuntimeException}
	 * @param exceptionMsg 异常信息 统一抛出的是{@link RuntimeException}
	 * @param function     objectMapper 转成 T
	 * @return T
	 */
	private static <T> T execute(String exceptionMsg, ObjectMapperFunc<T> function) {
		return execute(null, exceptionMsg, function);
	}
	
	/**
	 * json处理 统一抛出{@link RuntimeException}
	 * @param objectMapper 自定义的objectMapper, 如果不提供将使用{@link #OBJECT_MAPPER_THREAD_LOCAL}, 如果为null则使用默认的{@link #objectMapper}
	 * @param exceptionMsg 异常信息 统一抛出的是{@link RuntimeException}
	 * @param function     objectMapper 转成 T
	 * @return T
	 */
	private static <T> T execute(ObjectMapper objectMapper, String exceptionMsg, ObjectMapperFunc<T> function) {
		try {
			ObjectMapper executeObjectMapper = objectMapper;
			if (executeObjectMapper == null) {
				executeObjectMapper = OBJECT_MAPPER_THREAD_LOCAL.get();
			}
			if (executeObjectMapper == null) {
				executeObjectMapper = JSON.objectMapper;
			}
			return function.apply(executeObjectMapper);
		} catch (IOException e) {
			throw new RuntimeException(Optional.ofNullable(exceptionMsg).orElse("json operate failed"), e);
		}
	}
	
	/**
	 * 用于执行objectMapper操作
	 * @author sapluk
	 */
	@FunctionalInterface
	private interface ObjectMapperFunc<T> {
		/**
		 * function
		 * @param objectMapper objectMapper
		 * @return 执行结果
		 * @throws IOException          objectMapper方法可能抛出的异常
		 * @throws JsonParseException   objectMapper方法可能抛出的异常
		 * @throws JsonMappingException objectMapper方法可能抛出的异常
		 */
		T apply(ObjectMapper objectMapper) throws IOException, JsonParseException, JsonMappingException;
	}
	
	/**
	 * 无参无返回执行
	 */
	@FunctionalInterface
	public interface Procedure {
		void run();
	}
	
}
