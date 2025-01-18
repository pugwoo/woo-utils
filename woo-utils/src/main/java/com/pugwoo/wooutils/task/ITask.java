package com.pugwoo.wooutils.task;

/**
 * 2015年7月21日 11:17:23
 * 实际的任务逻辑实现这个接口：
 * 1) getRestCount() 返回待执行的任务数，当任务数等于小于0时，结束任务
 * 2) runStep() 由任务框架来调用,每调一次执行一个任务
 * 3) reset() 由任务框架来调用，认为是初始化数据
 */
public interface ITask {

	/**
	 * 获得剩下的任务记录数，当获取的任务数小于等于0时，认为任务结束。
	 * 每次执行runStep()前，该方法都会被调用一次，返回大于0才继续执行runStep()
	 */
	int getRestCount();

	/**
	 * 每次调用，执行单个记录。
	 * 【特别注意】如果要做到支持多线程，ITask的实现类，必须用变量支持多线程的并发调用。
	 * 请看test中MyTaskImpl例子。
	 * @return 成功返回true，失败返回false
	 */
	TaskResult runStep();
	
	/**
	 * 初始化Task，使得重跑时可以正常执行，这个方法会在每次start时调用
	 * 如果是resume调用则不掉这个接口
	 */
	void reset();
}
