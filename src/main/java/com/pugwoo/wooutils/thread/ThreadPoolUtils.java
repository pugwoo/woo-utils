package com.pugwoo.wooutils.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolUtils {

    /**
     * 创建一个线程池，一些默认配置：
     * 1）空闲线程存活时间为60秒
     * 2）拒绝策略：用默认，抛出RejectedExecutionException异常
     * @param coreSize
     * @param queueSize 任务排队队列最大长度
     * @param maxSize
     * @param threadNamePrefix 线程前缀名称
     */
    public static ThreadPoolExecutor createThreadPool(int coreSize, int queueSize, int maxSize, String threadNamePrefix) {
        return new ThreadPoolExecutor(
                coreSize, maxSize,
                60, // 空闲线程存活时间
                TimeUnit.SECONDS, // 存活时间单位
                queueSize <= 0 ? new SynchronousQueue<>() : new LinkedBlockingQueue<>(queueSize),
                new MyThreadFactory(threadNamePrefix)
        );
    }

    /**
     * 等待所有的future调用完成
     */
    public static List<?> waitAllFuturesDone(List<Future<?>> futures) {
        if (futures == null) {
            return new ArrayList<>();
        }
        List<Object> result = new ArrayList<>();
        for (Future<?> future : futures) {
            try {
                result.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    private static class MyThreadFactory implements ThreadFactory {

        private final AtomicInteger count = new AtomicInteger(1);
        private final String threadNamePrefix;

        public MyThreadFactory(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, threadNamePrefix + "-" + count.getAndIncrement());
        }
    }

}
