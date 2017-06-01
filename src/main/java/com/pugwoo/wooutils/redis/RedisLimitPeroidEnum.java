package com.pugwoo.wooutils.redis;

/**
 * @author nick
 */
public enum RedisLimitPeroidEnum {

	MINUTE("LIMIT_PEROID_MINUTE", "每分钟"),
	HOUR("LIMIT_PEROID_HOUR", "每小时"),
	DAY("LIMIT_PEROID_DAY", "每自然日"),
	WEEK_START_SUNDAY("LIMIT_PEROID_WEEK_START_SUNDAY", "每周(从周日开始)"),
	WEEK_START_MONDAY("LIMIT_PEROID_WEEK", "每周(从周一开始)"),
	MONTH("LIMIT_PEROID_MONTH", "每自然月"),
	YEAR("LIMIT_PEROID_YEAR", "每年"),
	PERMANENT("LIMIT_PEROID_PERMANENT", "永久");
	
	private String code;
	
	private String name;
	
	private RedisLimitPeroidEnum(String code, String name) {
		this.code = code;
		this.name = name;
	}
	
	public static RedisLimitPeroidEnum getByCode(String code) {
		for(RedisLimitPeroidEnum e : RedisLimitPeroidEnum.values()) {
			if(code == null && code == e.getCode() || code.equals(e.getCode())) {
				return e;
			}
		}
		return null;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
}
