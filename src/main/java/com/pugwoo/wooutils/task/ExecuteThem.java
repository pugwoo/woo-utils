package com.pugwoo.wooutils.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 2015年7月23日 14:34:51
 * 一个简易的以单线程的方式，控制多个任务多线程执行完之后再返回
 *
 * 使用方式：
 * 1. new一个ExecuteTime对象，然后把要执行的Runnable或Callable对象放入executeTime对象中
 * 2. 调用waitAllTerminate方法等待所有任务执行完成
 */
public class ExecuteThem {
	
	private ExecutorService executorService;
	
	private List<Future<?>> futures = new ArrayList<Future<?>>();
	
	private List<Exception> exceptions = new ArrayList<Exception>();
	
	public ExecuteThem() {
		executorService = Executors.newFixedThreadPool(10);
	}
	
	public ExecuteThem(int nThreads) {
		executorService = Executors.newFixedThreadPool(nThreads);
	}

	public void add(Runnable... runnables) {
		for(Runnable runable : runnables) {
			executorService.execute(runable);
		}
	}
	
	public void add(Callable<?>... callables) {
		for(Callable<?> callable : callables) {
			futures.add(executorService.submit(callable));
		}
	}
	
	/**
	 * 等待所有的任务执行结束并返回结果
	 * 对于Runnable没有返回值所以不会在这里面；
	 * 对于Callable的返回值按顺序返回，返回已经执行的任务的值，如果执行任务抛出异常，那么返回值为null
	 * 同时线程保存下异常
	 * 
	 * 主要：如果线程池被interrupted，那么线程池会放弃后面未执行的任务。
	 * @return
	 */
	public List<Object> waitAllTerminate() {
		return waitAllTerminate(Long.MAX_VALUE);
	}
	
	public List<Object> waitAllTerminate(long waitSeconds) {
		executorService.shutdown();
		try {
			executorService.awaitTermination(waitSeconds, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			executorService.shutdownNow();
		}
		
		List<Object> results = new ArrayList<Object>();
		for(Future<?> future : futures) {
			try {
				results.add(future.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
				results.add(null);
				exceptions.add(e);
			} catch (ExecutionException e) {
				e.printStackTrace();
				results.add(null);
				exceptions.add(e);
			}
		}
		
		return results;
	}
}
