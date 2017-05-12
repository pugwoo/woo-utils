package com.pugwoo.wooutils.task;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 2015年7月21日 11:17:16
 * 按最简单的方式：每次只能有一个task
 * 
 * TODO 增加pause方法
 * 
 * @author pugwoo
 */
public class EasyRunTask {
	
	/**用于同步sync*/
	private final EasyRunTask that = this;

	/**要执行的任务实现*/
	private final ITask task;
	/**执行状态*/
	private StatusEnum status = StatusEnum.NEW;
	/**每次同时多线程指定的任务数，需要一批一批来，每批作为停止的单位*/
	private final int concurrentNum;
	
	/**抛出的异常记录[线程安全]*/
	private List<Throwable> exceptions = new Vector<Throwable>();
	/**任务总数[线程安全]*/
	private AtomicInteger total = new AtomicInteger(0);
	/**执行的任务总数[线程安全]*/
	private AtomicInteger processed = new AtomicInteger(0);
	/**执行成功的任务总数[线程安全]*/
	private AtomicInteger success = new AtomicInteger(0);
	/**执行失败的任务总数[线程安全]*/
	private AtomicInteger fail = new AtomicInteger(0);
	
	/**
	 * 状态枚举
	 */
	public static enum StatusEnum {
		/**就绪*/
		NEW, 
		/**运行中*/
		RUNNING,
		/**终止允许中*/
		STOPPING,
		/**终止*/
		STOPPED,
		/**执行完成*/
		FINISHED
	}
	
	public EasyRunTask(ITask task) {
		this.task = task;
		this.concurrentNum = 1;
	}
	
	public EasyRunTask(ITask task, int concurrentNum) {
		this.task = task;
		this.concurrentNum = concurrentNum;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("task:").append(task == null ? "null" : task.getClass().getName()).append(",");
		sb.append("status:").append(status).append(",");
		sb.append("total:").append(total).append(",");
		sb.append("processed:").append(processed).append(",");
		sb.append("success:").append(success).append(",");
		sb.append("fail:").append(fail).append(",");
		sb.append("exceptions:").append(exceptions.size());
		// TODO 打印出第一个异常堆栈
		return sb.toString();
	}
	
	/**
	 * 启动任务
	 * @return
	 */
	public synchronized TaskResult start() {
		return run(true);
	}
	
	/**
	 * 停止之后恢复任务
	 * @return
	 */
	public synchronized TaskResult resume() {
		return run(false);
	}
	
	/**
	 * 停止任务
	 * @return
	 */
	public synchronized TaskResult stop() {
		if(status == StatusEnum.RUNNING) {
			status = StatusEnum.STOPPING;
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
		if(status == StatusEnum.RUNNING || status == StatusEnum.STOPPING) {
			return new TaskResult(false, "cannot start when running");
		}
		if(task == null) {
			return new TaskResult(false, "task is not assigned");
		}
		
		if(reset) {
			task.reset();
			total.set(0);
			processed.set(0);
			success.set(0);
			fail.set(0);
			exceptions.clear();
		}
		status = StatusEnum.RUNNING;
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					synchronized (that) { // 请求停止
						if(status == StatusEnum.STOPPING) {
							status = StatusEnum.STOPPED;
							return;
						}
					}
					
					int restCount = getRestCount();
					if(getRestCount() <= 0) {
						synchronized (that) { // 结束任务
							status = StatusEnum.FINISHED;
						}
						return;
					}
					
					// 多线程执行任务
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
			}
		}, "EasyRunTaskExecute").start();

		return new TaskResult(true);
	}

	/**
	 * 获得当前的任务状态
	 * @return
	 */
	public StatusEnum getStatus() {
		return status;
	}
	/**
	 * 获得当前的异常
	 * @return
	 */
	public List<Throwable> getExceptions() {
		return exceptions;
	}
	/**
	 * 获得所有的记录数
	 * @return
	 */
	public int getTotal() {
		return total.get();
	}
	/**
	 * 获得已处理的记录数
	 * @return
	 */
	public int getProcessed() {
		return processed.get();
	}
	/**
	 * 获得成功的记录数
	 * @return
	 */
	public int getSuccess() {
		return success.get();
	}
	/**
	 * 获得失败的记录数
	 * @return
	 */
	public int getFail() {
		return fail.get();
	}
	
}
