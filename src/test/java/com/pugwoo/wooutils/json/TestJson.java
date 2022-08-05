package com.pugwoo.wooutils.json;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TestJson {
	
	/** 默认的objectMapper */
	private final ObjectMapper defaultObjectMapper = new ObjectMapper();
	
	public static class MyClass {
		private Map<String, Object> map;
		public Map<String, Object> getMap() {
			return map;
		}
		public void setMap(Map<String, Object> map) {
			this.map = map;
		}
	}

	public static class Student {
		@JsonSetter(nulls= Nulls.AS_EMPTY) // 这个是生效的，可以使得当为null时，设置为空字符串
		private String name = "default"; // 这里设置的默认值，会被json里的null值覆盖；但是如果json里面没有这个属性，那就是默认值了；所以这里还做不到，如果json里是null，怎么让它用默认值，而非转成null；除非改写setter方法，但也麻烦

		@JsonSetter(nulls= Nulls.AS_EMPTY) // 这个有效，当是null时，值为0
		private Integer age = 1;

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Integer getAge() {
			return age;
		}
		public void setAge(Integer age) {
			this.age = age;
		}
	}

	@Test
	public void testNullValue() {
		String json = "{\"name\":null,\"age\":null}";
		Student student = JSON.parse(json, Student.class);
		System.out.println(student.getName() + "," + student.getAge());
		assert student.getName().isEmpty();
		assert student.getAge() == 0;
	}
	
	@Test
	public void toJsonTest() {
		System.out.println("\n================ 转json示例");
		
		Date date = new Date();
		System.out.println("  date: " + date);
		System.out.println("toJson: " + JSON.toJson(new Date()));
		
		Map<String, Object> map = new HashMap<>();
		map.put("name", "nick");
		map.put("age", null);
		map.put(null, null);
		System.out.println(JSON.toJson(map));
		System.out.println(JSON.toJson(JSON.parse(JSON.toJson(map))));
	}
	
	@Test
	public void parseTest() {
		System.out.println("\n================ 解析json示例");
		
		System.out.println(JSON.parse("\"2017-03-03 15:34\"", Date.class));
		System.out.println(JSON.parse("\"2017年3月30日\"", Date.class));
		System.out.println(JSON.parse("\"  \"", Date.class));
		
		System.out.println();
		
		MyClass myclass = JSON.parse("{\"map\":\"\"}", MyClass.class);
		System.out.println(JSON.toJson(myclass));
	}
	
	@Test
	public void parseGenericTest() {
		System.out.println("\n================ 解析json示例-泛型1");
		String json1 = "[\"20180102\", \"20180306\"]";
		@SuppressWarnings("unchecked")
		List<Date> list = JSON.parse(json1, List.class, Date.class);
		System.out.println(list);
		
		System.out.println("\n================ 解析json示例-泛型2");
		String json2 = "{\"arr\":\"20180102\"}";
		@SuppressWarnings("unchecked")
		Map<String, Date> map = JSON.parse(json2, Map.class, String.class, Date.class);
		System.out.println(map);
	}
	
	@Test
	public void parseReferenceTest() {
		System.out.println("\n================ 解析json示例-类型引用1");
		String json1 = "[\"20180102\", \"20180306\"]";
		List<Date> list = JSON.parse(json1, new TypeReference<List<Date>>() {});
		System.out.println(list);
		
		System.out.println("\n================ 解析json示例-类型引用2");
		String json2 = "{\"arr\":\"20180102\"}";
		Map<String, Date> map = JSON.parse(json2, new TypeReference<Map<String, Date>>() {});
		System.out.println(map);
	}
	
	@Test
	public void testAll() {
		System.out.println("\n=========================================================== 依次测试每一个方法");
		String mapJson = "{\"name\":\"hello\"}";
		String listMapJson = "[{\"name\":\"hello1\"}, {\"name\":\"hello2\"}]";
		String mapJsonError = "{name:hello}";
		
		System.out.println("\n -------------------------------------------- common ");
		
		System.out.println("\n -------------------------------------------- 反序列化 json -> object ");
		
		System.out.println("\n ---- Object JSON.parse(String)");
		Object object1 = JSON.parse(mapJson);
		Object object2 = JSON.useThreadObjectMapper(defaultObjectMapper, () -> JSON.parse(mapJson));
		Object object3;
		try {
			JSON.setThreadObjectMapper(defaultObjectMapper);
			object3 = JSON.parse(mapJson);
		} finally {
			JSON.removeThreadObjectMapper();
		}
		System.out.println(object1);
		System.out.println(object2);
		System.out.println(object3);
		
		System.out.println("\n ---- Object JSON.parse(String, Class)");
		Map map1 = JSON.parse(mapJson, Map.class);
		System.out.println(map1);
		JSON.useThreadObjectMapper(defaultObjectMapper, () -> {
			Map map2 = JSON.parse(mapJson, Map.class);
			System.out.println(map2);
		});
		
		System.out.println("\n ---- T JSON.parse(String, TypeReference)");
		Map<String, String> mapStringString1 = JSON.parse(mapJson, new TypeReference<Map<String, String>>() {});
		Map<String, String> mapStringString2 = JSON.useThreadObjectMapper(defaultObjectMapper, () ->
				JSON.parse(mapJson, new TypeReference<Map<String, String>>() {})
		);
		System.out.println(mapStringString1);
		System.out.println(mapStringString2);
		// 变量类型的实例不存在，因此Map<String, String> 符合 Map<String, Long>
		// Map<String, Long> mapStringString3 = JSON.parse(mapJson, new TypeReference<Map<String, String>>() {});
		
		System.out.println("\n ---- Map<String, Object> JSON.parseToMap(String) ");
		Map<String, Object> parseToMap1 = JSON.parseToMap(mapJson);
		Map<String, Object> parseToMap2 = JSON.useThreadObjectMapper(defaultObjectMapper, () -> JSON.parseToMap(mapJson));
		System.out.println(parseToMap1);
		System.out.println(parseToMap2);
		
		System.out.println("\n ---- List<Map<String, Object>> parseToListMap(String) ");
		String parseToListMap = "[{\"name\":\"hello1\"}, {\"name\":\"hello2\"}]";
		List<Map<String, Object>> parseToListMap1 = JSON.parseToListMap(parseToListMap);
		List<Map<String, Object>> parseToListMap2 = JSON.useThreadObjectMapper(defaultObjectMapper, () -> JSON.parseToListMap(parseToListMap));
		System.out.println(parseToListMap1);
		System.out.println(parseToListMap2);
		
		System.out.println("\n ---- List<T> parseToList(String, Class) ");
		String parseToListClassJson = "[123,456,789]";
		List<Long> parseToList01 = JSON.parseToList(parseToListClassJson, Long.class);
		List<Long> parseToList02 = JSON.useThreadObjectMapper(defaultObjectMapper, () ->
				JSON.parseToList(parseToListClassJson, Long.class)
		);
		System.out.println(parseToList01);
		System.out.println(parseToList02);
		System.out.println(parseToList01.get(0).getClass());
		System.out.println(parseToList02.get(0).getClass());
		
		// 默认是会解析为int
		List parseToList03 = JSON.parse(parseToListClassJson, List.class);
		System.out.println(parseToList03);
		System.out.println(parseToList03.get(0).getClass());
		
		System.out.println("\n ---- List<T> parseToList(String, TypeReference) ");
		String parseToListTypeReferenceJsonLong = "[123,456,789]";
		List<Long> parseToListTypeReferenceLong01 = JSON.parseToList(parseToListTypeReferenceJsonLong, new TypeReference<Long>() {});
		List<Long> parseToListTypeReferenceLong02 = JSON.useThreadObjectMapper(defaultObjectMapper, () ->
				JSON.parseToList(parseToListTypeReferenceJsonLong, new TypeReference<Long>() {})
		);
		System.out.println(parseToListTypeReferenceLong01);
		System.out.println(parseToListTypeReferenceLong02);
		System.out.println(parseToListTypeReferenceLong01.get(0).getClass());
		System.out.println(parseToListTypeReferenceLong02.get(0).getClass());
		
		String parseToListTypeReferenceJsonMap = "[{\"name\":\"hello1\"}, {\"name\":\"hello2\"}]";
		List<Map<String, Object>> parseToListTypeReferenceMap01 = JSON.parseToList(parseToListTypeReferenceJsonMap, new TypeReference<Map<String, Object>>() {});
		List<Map<String, Object>> parseToListTypeReferenceMap02 = JSON.useThreadObjectMapper(defaultObjectMapper, () ->
				JSON.parseToList(parseToListTypeReferenceJsonMap, new TypeReference<Map<String, Object>>() {})
		);
		System.out.println(parseToListTypeReferenceMap01);
		System.out.println(parseToListTypeReferenceMap02);
		System.out.println(parseToListTypeReferenceMap01.get(0).getClass());
		System.out.println(parseToListTypeReferenceMap02.get(0).getClass());
		
		System.out.println("\n ---- List<T> parse(String, Class, Class[]) ");
		String parseGenericClassesJson = "[\"20180102\", \"20180306\"]";
		List<Date> parseGenericClasses = JSON.parse(parseGenericClassesJson, List.class, Date.class);
		System.out.println(parseGenericClasses);
		System.out.println(parseGenericClasses.get(0).getClass());
		
		System.out.println("\n ---- ObjectNode parseObject(String) ");
		ObjectNode jsonNodes = JSON.parseObject(mapJson);
		JsonNode nameJsonNode = jsonNodes.get("name");
		String name = nameJsonNode.asText();
		assert "hello".equals(name);
		
		System.out.println("\n ---- List<JsonNode> parseArray(String) ");
		List<JsonNode> jsonNodesList = JSON.parseArray(listMapJson);
		JsonNode jsonNode = jsonNodesList.get(0);
		assert "hello1".equals(Optional.ofNullable(jsonNode).map(item -> item.get("name")).map(JsonNode::asText).orElse(null));
		
		
		System.out.println("\n -------------------------------------------- 反序列化 json -> object ");
		
		Map<String, Object> map = new HashMap<>();
		map.put("name", "hello");
		map.put("date", new Date());
		
		System.out.println("\n ---- toJson ");
		System.out.println(JSON.toJson(map));
		System.out.println(JSON.useThreadObjectMapper(defaultObjectMapper, () -> JSON.toJson(map)));
		
		System.out.println("\n ---- toJsonFormatted ");
		System.out.println(JSON.toJsonFormatted(map));
		System.out.println(JSON.useThreadObjectMapper(defaultObjectMapper, () -> JSON.toJsonFormatted(map)));
		
		System.out.println("\n -------------------------------------------- 其他 ");
		System.out.println("\n ---- toMap ");
		Student student = new Student();
		student.setName("helloWorld");
		student.setAge(16);
		Map<String, Object> studentMap = JSON.toMap(student);
		System.out.println(studentMap);
		
		System.out.println("\n -------------------------------------------- clone 见 TestJsonClone ");
		
		System.out.println("\n -------------------------------------------- objectMapper ");
		Assert.assertNotNull(JSON.getObjectMapper());
		JSON.setGlobalObjectMapper(JSON.getObjectMapper());
		Assert.assertThrows(IllegalArgumentException.class, () -> JSON.setGlobalObjectMapper(null));
		
		
		
	}

}
