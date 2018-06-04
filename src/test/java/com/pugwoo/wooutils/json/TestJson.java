package com.pugwoo.wooutils.json;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestJson {
	
	public static class MyClass {
		private Map<String, Object> map;
		public Map<String, Object> getMap() {
			return map;
		}
		public void setMap(Map<String, Object> map) {
			this.map = map;
		}
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		Date date = JSON.parse("\"2017-03-03 15:34\"", Date.class);
		System.out.println(date);
		
		date = JSON.parse("\"2017年3月30日\"", Date.class);
		System.out.println(date);
		
		date = JSON.parse("\"  \"", Date.class);
		System.out.println(date);
		
		System.out.println(JSON.toJson(new Date()));
		
		Map<String, Object> map = new HashMap<>();
		map.put("name", "nick");
		map.put("age", null);
		map.put(null, null);
		System.out.println(JSON.toJson(map));
		System.out.println(JSON.toJson(JSON.parse(JSON.toJson(map))));
		
		// ==== 泛型解析示例
		
		String json1 = "[\"20180102\", \"20180306\"]";
		List<Date> list = JSON.parse(json1, List.class, Date.class);
		System.out.println(list);
		
		String json2 = "{\"arr\":\"20180102\"}";
		Map<String, Date> m = JSON.parse(json2, Map.class, String.class, Date.class);
		System.out.println(m);
		
		// ====== 
		MyClass myclass = JSON.parse("{\"map\":\"\"}", MyClass.class);
		System.out.println(JSON.toJson(myclass));
		
	}
	
}
