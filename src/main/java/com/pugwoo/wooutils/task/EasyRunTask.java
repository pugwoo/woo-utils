package com.pugwoo.wooutils.task;

import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 2015年7月21日 11:17:16
 * 简单的可以控制开始、停止、恢复或重新开始的任务控制框架。
 * 
 * 关于暂停：等价于 start - stop - resume
 * 重新开始：等价于 start - stop - start
 * 
 * @author pugwoo
 */
public class EasyRunTask {
	
	/**用于同步sync*/
	private final EasyRunTask that = this;

	/**要执行的任务实现*/
	private final ITask task;
	/**执行状态*/
	private TaskStatusEnum status = TaskStatusEnum.NEW;
	/**每次同时多线程指定的任务数，需要一批一批来，每批作为停止的单位*/
	private final int concurrentNum;
	
	/**抛出的异常记录[线程安全]*/
	private final List<Throwable> exceptions = new Vector<>();
	/**任务总数[线程安全]*/
	private final AtomicInteger total = new AtomicInteger(0);
	/**执行的任务总数[线程安全]*/
	private final AtomicInteger processed = new AtomicInteger(0);
	/**执行成功的任务总数[线程安全]*/
	private final AtomicInteger success = new AtomicInteger(0);
	/**执行失败的任务总数[线程安全]*/
	private final AtomicInteger fail = new AtomicInteger(0);
	
	/**任务开始时间*/
	private Date startTime;
	/**任务结束时间*/
	private Date endTime;
		
	public EasyRunTask(ITask task) {
		this.task = task;
		this.concurrentNum = 1;
	}
	
	public EasyRunTask(ITask task, int concurrentNum) {
		this.task = task;
		this.concurrentNum = concurrentNum;
	}
	
	/**
	 * 启动任务
	 * @return 操作结果
	 */
	public synchronized TaskResult start() {
		return run(true);
	}
	
	/**
	 * 停止之后恢复任务
	 * @return 操作结果
	 */
	public synchronized TaskResult resume() {
		return run(false);
	}
	
	/**
	 * 停止任务
	 * @return 操作结果
	 */
	public synchronized TaskResult stop() {
		if(status == TaskStatusEnum.RUNNING) {
			status = TaskStatusEnum.STOPPING;
			return new TaskResult(true);
		}
		return new TaskResult(false, "stop must at running status");
	}
	
	/**查询剩余的任务数，如果有异常，也返回0，返回0表示没有更多任务了*/
	private int getRestCount() {
		try {
			return task.getRestCount();
		} catch (Throwable e) {
			exceptions.add(e);
			return 0;
		}
	}

	private synchronized TaskResult run(boolean reset) {
		if(status == TaskStatusEnum.RUNNING || status == TaskStatusEnum.STOPPING) {
			return new TaskResult(false, "cannot start when running");
		}
		if(task == null) {
			return new TaskResult(false, "task is not assigned");
		}
		
		if(reset) {
			startTime = new Date();
			task.reset();
			total.set(0);
			processed.set(0);
			success.set(0);
			fail.set(0);
			exceptions.clear();
		}
		status = TaskStatusEnum.RUNNING;
		
		new Thread(() -> {
			while(true) {
				synchronized (that) { // 请求停止
					if(status == TaskStatusEnum.STOPPING) {
						status = TaskStatusEnum.STOPPED;
						endTime = new Date();
						return;
					}
				}

				int restCount = getRestCount();
				if(getRestCount() <= 0) {
					synchronized (that) { // 结束任务
						status = TaskStatusEnum.FINISHED;
						endTime = new Date();
					}
					return;
				}

				// 多线程执行任务，实际上，是一批一批地去执行，这样才能中途控制其停下
				int nThreads = Math.min(restCount, concurrentNum);
				ExecuteThem executeThem = new ExecuteThem(nThreads);
				for(int i = 0; i < nThreads; i++) {
					executeThem.add(new Runnable() {
						@Override
						public void run() {
							try {
								TaskResult result = task.runStep();
								if(result == null || !result.isSuccess()) {
									fail.incrementAndGet();
								} else {
									success.incrementAndGet();
								}
							} catch (Throwable e) {
								exceptions.add(e);
								fail.incrementAndGet();
							} finally {
								processed.incrementAndGet();
							}
						}
					});
				}
				executeThem.waitAllTerminate();
				total.set(processed.get() + getRestCount());
			}
		}, "EasyRunTaskExecute").start();

		return new TaskResult(true);
	}

	/**
	 * 获得当前的任务状态
	 */
	public TaskStatusEnum getStatus() {
		return status;
	}
	/**
	 * 获得当前的异常
	 */
	public List<Throwable> getExceptions() {
		return exceptions;
	}
	/**
	 * 获得所有的记录数
	 */
	public int getTotal() {
		return total.get();
	}
	/**
	 * 获得已处理的记录数
	 */
	public int getProcessed() {
		return processed.get();
	}
	/**
	 * 获得成功的记录数
	 */
	public int getSuccess() {
		return success.get();
	}
	/**
	 * 获得失败的记录数
	 */
	public int getFail() {
		return fail.get();
	}
	/**
	 * 获得任务执行开始时间
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * 获得任务执行结束时间
	 */
	public Date getEndTime() {
		return endTime;
	}
	
}
