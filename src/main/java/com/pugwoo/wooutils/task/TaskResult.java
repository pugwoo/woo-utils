package com.pugwoo.wooutils.task;

/**
 * 2015年7月21日 13:59:45
 */
public class TaskResult {

	/**
	 * 是否成功
	 */
	private boolean success;

	/**
	 * 返回码
	 */
	private String code;

	/**
	 * 错误消息
	 */
	private String message;

	public TaskResult() {
	}
	
	public TaskResult(boolean success) {
		this.success = success;
	}
	
	public TaskResult(boolean success, String message) {
		this.success = success;
		this.message = message;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
