package com.pugwoo.wooutils.task;

/**
 * 执行任务状态
 * @author nick
 */
public enum TaskStatusEnum {
	
	/**就绪*/
	NEW("NEW", "就绪"),
	
	/**运行中*/
	RUNNING("RUNNING", "运行中"),
	
	/**终止中*/
	STOPPING("STOPPING", "终止中"),
	
	/**已终止*/
	STOPPED("STOPPED", "已终止"),
	
	/**执行完成*/
	FINISHED("FINISHED", "执行完成"),
	
	;
	
	private String code;
	
	private String name;
	
	private TaskStatusEnum(String code, String name) {
		this.code = code;
		this.name = name;
	}
	
	public static TaskStatusEnum getByCode(String code) {
		for(TaskStatusEnum e : TaskStatusEnum.values()) {
			if(code == e.getCode() || code != null && code.equals(e.getCode())) {
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
