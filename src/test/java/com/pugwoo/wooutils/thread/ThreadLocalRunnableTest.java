package com.pugwoo.wooutils.thread;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 锟斤拷 <br>
 * 2020/07/04 <br>
 *
 */
public class ThreadLocalRunnableTest {
    
    public static ThreadLocal<String> threadLocal1 = new ThreadLocal<>();
    public static ThreadLocal<Integer> threadLocal2 = new ThreadLocal<>();
    public static ThreadLocal<Integer> threadLocal3 = new ThreadLocal<>();
    
    @Test
    public void test() {
        ThreadLocalContent.addCommonThreadLocal(threadLocal3);
        
        threadLocal1.set("1");
        threadLocal2.set(2);
        threadLocal3.set(3);
        
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(new ThreadLocalRunnable(ThreadLocalRunnableTest::print, threadLocal1));
        executor.submit(new ThreadLocalRunnable(ThreadLocalRunnableTest::print, threadLocal2));
        executor.submit(new ThreadLocalRunnable(ThreadLocalRunnableTest::print));
        executor.submit(new ThreadLocalRunnable(ThreadLocalRunnableTest::print, threadLocal1, threadLocal2));
        executor.submit(new ThreadLocalRunnable(ThreadLocalRunnableTest::print, threadLocal2, threadLocal3));
        executor.submit(new ThreadLocalRunnable(ThreadLocalRunnableTest::print, threadLocal1, threadLocal3));
        executor.submit(new ThreadLocalRunnable(ThreadLocalRunnableTest::print, threadLocal1, threadLocal2, threadLocal3));
        executor.shutdown();
        
        print();
    }
    
    private static void print() {
        String threadName = Thread.currentThread().getName();
        System.out.println(threadName + ":" + threadLocal1.get()
                + "   " + threadLocal2.get()
                + "   " + threadLocal3.get()
        );
    }
}
