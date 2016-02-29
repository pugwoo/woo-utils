package com.pugwoo.wooutils.task;

import java.util.Random;

/**
 * 这是一个示例的任务实现:
 * 假设有100个任务要执行
 * 
 * 2016年2月29日 19:44:02
 */
public class MyTaskImpl implements ITask {
	
	Integer count = null;

	@Override
	public int getRestCount() {
		// 任务框架开始的时候，会先调getRestCount看有多少个任务要查询
		// 如果任务存在DB时，这里相当于查询count(*)任务数
		if(count == null) {
			count = 100;
		}
		return count;
	}

	@Override
	public TaskResult runStep() {
		// 任务框架调用这个一次相当于执行一次任务
		System.out.println("system is handle task " + count);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		count--;
		
		// 告诉任务框架，任务处理成功了还是失败，目前失败也不会重试的，只是记录下来
		return new TaskResult(new Random().nextDouble() > 0.5);
	}

	@Override
	public void reset() {
		count = null; // 类变量初始化，这个任务开始时不会调，只有stop后再启动再调
	}

}
