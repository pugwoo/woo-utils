package com.pugwoo.wooutils.thread;

import com.pugwoo.wooutils.log.MDCUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 该工具类创建的线程池可以自动继承父线程的MDC上下文。
 */
public class ThreadPoolUtils {

    public static class BlockingRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
                // 将任务重新放入队列中
                executor.getQueue().put(r);
            } catch (InterruptedException e) {
                // 线程被中断的处理
                Thread.currentThread().interrupt();
                throw new RejectedExecutionException("Interrupted while submitting task", e);
            }
        }
    }

    /**
     * 创建一个线程池，一些默认配置：
     * 1）空闲线程存活时间为60秒
     * 2）拒绝策略：用默认，抛出RejectedExecutionException异常
     * <br>
     * 说明：该工具类创建的线程池可以自动继承父线程的MDC上下文。
     * <br>
     * @param coreSize 核心线程数，可以理解为线程池在开始排队时，达到的最大线程数
     * @param queueSize 任务排队队列最大长度
     * @param maxSize 最大线程数，当等待队列满了之后，线程数会继续加大到该值
     * @param threadNamePrefix 线程前缀名称
     */
    public static ThreadPoolExecutor createThreadPool(int coreSize, int queueSize, int maxSize, String threadNamePrefix) {
        // 参数验证
        if (coreSize < 0) {
            throw new IllegalArgumentException("coreSize cannot be negative: " + coreSize);
        }
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be positive: " + maxSize);
        }
        if (coreSize > maxSize) {
            throw new IllegalArgumentException("coreSize cannot be greater than maxSize: " + coreSize + " > " + maxSize);
        }
        if (threadNamePrefix == null) {
            threadNamePrefix = "thread-pool";
        }
        
        return new ThreadPoolExecutor(
                coreSize, maxSize,
                60, // 空闲线程存活时间
                TimeUnit.SECONDS, // 存活时间单位
                queueSize <= 0 ? new SynchronousQueue<>() : new LinkedBlockingQueue<>(queueSize),
                new MyThreadFactory(threadNamePrefix)
        );
    }

    /**
     * 创建一个线程池，一些默认配置：
     * 1）空闲线程存活时间为60秒
     * 2）拒绝策略：用默认，抛出RejectedExecutionException异常
     * <br>
     * 说明：该工具类创建的线程池可以自动继承父线程的MDC上下文。
     * <br>
     * @param coreSize 核心线程数，可以理解为线程池在开始排队时，达到的最大线程数
     * @param queueSize 任务排队队列最大长度
     * @param maxSize 最大线程数，当等待队列满了之后，线程数会继续加大到该值
     * @param threadNamePrefix 线程前缀名称
     * @param isBlockingWhenQueueFull 当队列满了之后，是否阻塞等待，true时等待，false则抛出RejectedExecutionException异常
     */
    public static ThreadPoolExecutor createThreadPool(int coreSize, int queueSize, int maxSize, String threadNamePrefix,
                                                      boolean isBlockingWhenQueueFull) {
        // 参数验证
        if (coreSize < 0) {
            throw new IllegalArgumentException("coreSize cannot be negative: " + coreSize);
        }
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be positive: " + maxSize);
        }
        if (coreSize > maxSize) {
            throw new IllegalArgumentException("coreSize cannot be greater than maxSize: " + coreSize + " > " + maxSize);
        }
        if (threadNamePrefix == null) {
            threadNamePrefix = "thread-pool";
        }

        if (isBlockingWhenQueueFull) {
            RejectedExecutionHandler handler = new BlockingRejectedExecutionHandler();
            return new ThreadPoolExecutor(
                    coreSize, maxSize,
                    60, // 空闲线程存活时间
                    TimeUnit.SECONDS, // 存活时间单位
                    queueSize <= 0 ? new SynchronousQueue<>() : new LinkedBlockingQueue<>(queueSize),
                    new MyThreadFactory(threadNamePrefix),
                    handler
            );
        } else {
            return new ThreadPoolExecutor(
                    coreSize, maxSize,
                    60, // 空闲线程存活时间
                    TimeUnit.SECONDS, // 存活时间单位
                    queueSize <= 0 ? new SynchronousQueue<>() : new LinkedBlockingQueue<>(queueSize),
                    new MyThreadFactory(threadNamePrefix)
            );
        }
    }

    /**
     * 等待所有的future调用完成
     */
    public static <T> List<T> waitAllFuturesDone(List<Future<T>> futures) {
        if (futures == null) {
            return new ArrayList<>();
        }
        List<T> result = new ArrayList<>();
        for (Future<T> future : futures) {
            try {
                result.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * 关闭线程池并等待所有的线程执行完毕（包括在队列中排队的任务）
     */
    public static void shutdownAndWaitAllTermination(ThreadPoolExecutor threadPoolExecutor) {
        threadPoolExecutor.shutdown();
        while(true) {
            try {
                if (threadPoolExecutor.awaitTermination(24, TimeUnit.HOURS)) {
                    break;
                }
            } catch (InterruptedException ignored) {
            }
        }
    }

    private static class MyThreadFactory implements ThreadFactory {

        private final AtomicInteger count = new AtomicInteger(1);
        private final String threadNamePrefix;

        public MyThreadFactory(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(MDCUtils.withMdc(r), threadNamePrefix + "-" + count.getAndIncrement());
        }
    }

}
