package com.pugwoo.wooutils.task;

/**
 * 2016年2月29日 19:49:29
 * 演示怎样用任务框架来跑任务和控制停止
 */
public class TestMyTaskImpl {

	public static void main(String[] args) throws Exception {
		ITask task = new MyTaskImpl();
		EasyRunTask easyRunTask = new EasyRunTask(task, 3);
		
		easyRunTask.start(); // 启动任务框架，任务开始执行
		
		Thread.sleep(10000); // 等待10秒后
		System.out.println(easyRunTask.getStatus());
		
		easyRunTask.stop(); // 控制任务框架停止
		System.out.println(easyRunTask.getStatus());
		
		Thread.sleep(2000); // 等待框架结束
		System.out.println(easyRunTask.getStatus());
	}
}
