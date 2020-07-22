package com.pugwoo.wooutils.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 2015年7月23日 14:34:51
 * 一个简易的以单线程的方式，控制多个任务多线程执行完之后再返回。
 * 
 * 默认线程池大小为10个，可以修改。
 *
 * 使用方式：
 * 1. new一个ExecuteTime对象，然后把要执行的Runnable或Callable对象放入executeTime对象中
 * 2. 调用waitAllTerminate方法等待所有任务执行完成
 * 
 * 【注】每个ExecuteThem调用waitAllTerminate之后，就不能再add增加任务了。
 */
public class ExecuteThem {

    /**
     * 关于使用了Executors.newFixedThreadPool说明：
     * Executors.newFixedThreadPool有文章提示说固定线程池的缓存队列是无限长的，有内存溢出的风险。
     * 经过测试，320万的任务堆积，大概占用139M内存，即每个任务占用约50字节。
     * 所以任务堆积几百万本身产生的问题就要大于内存问题，所以这里仍然使用Executors.newFixedThreadPool，不会有问题。
     */
	private ExecutorService executorService;
	
	private List<Future<?>> futures = new ArrayList<>();
	
	private List<Exception> exceptions = new ArrayList<>();

	public ExecuteThem() {
		executorService = Executors.newFixedThreadPool(10,
				new MyThreadFactory("exec-them"));
	}

    /**
     * @param nThreads 指定线程池最大线程数
     */
	public ExecuteThem(int nThreads) {
		executorService = Executors.newFixedThreadPool(nThreads,
				new MyThreadFactory("exec-them"));
	}

	/**
	 * @param nThreads 指定线程池最大线程数
	 * @param threadNamePrefix 线程池的前缀
	 */
	public ExecuteThem(int nThreads, String threadNamePrefix) {
		executorService = Executors.newFixedThreadPool(nThreads,
				new MyThreadFactory(threadNamePrefix));
	}

	public void add(Runnable... runnables) {
		for(Runnable runable : runnables) {
			executorService.submit(runable);
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
			exceptions.add(e);
		} finally {
			executorService.shutdownNow();
		}
		
		List<Object> results = new ArrayList<>();
		for(Future<?> future : futures) {
			try {
				results.add(future.get());
			} catch (InterruptedException e) {
				results.add(null);
				exceptions.add(e);
			} catch (ExecutionException e) {
				results.add(null);
				exceptions.add(e);
			}
		}
		
		return results;
	}


	private static class MyThreadFactory implements ThreadFactory {

		private AtomicInteger count = new AtomicInteger(1);

		private String threadNamePrefix;

		public MyThreadFactory(String threadNamePrefix) {
			this.threadNamePrefix = threadNamePrefix;
		}

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, threadNamePrefix + "-" + count.getAndIncrement());
		}

	}
}
