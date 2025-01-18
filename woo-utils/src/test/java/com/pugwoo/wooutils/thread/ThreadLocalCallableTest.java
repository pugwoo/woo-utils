package com.pugwoo.wooutils.thread;

import com.pugwoo.wooutils.collect.ListUtils;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author 锟斤拷 <br>
 *
 * 2020/07/04 <br>
 *
 */
public class ThreadLocalCallableTest {
    
    public static ThreadLocal<String> threadLocal1 = new ThreadLocal<>();
    public static InheritableThreadLocal<String> threadLocal11 = new InheritableThreadLocal<>();
    public static ThreadLocal<Integer> threadLocal2 = new ThreadLocal<>();
    public static ThreadLocal<Integer> threadLocal3 = new ThreadLocal<>();
    
    
    @Test
    public void test() throws Exception {
        String threadName = Thread.currentThread().getName();
        
        ThreadLocalContent
//                .addCommonThreadLocal(threadLocal3)
                .addCommonThreadLocal(threadLocal2);
        
        threadLocal1.set("1");
        threadLocal2.set(2);
        threadLocal3.set(3);
    
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        List<Future<String>> futures = executor.invokeAll(ListUtils.newArrayList(
                new ThreadLocalCallable<>(ThreadLocalCallableTest::print, threadLocal1),
                new ThreadLocalCallable<>(ThreadLocalCallableTest::print, threadLocal2),
                new ThreadLocalCallable<>(ThreadLocalCallableTest::print, threadLocal3),
                new ThreadLocalCallable<>(ThreadLocalCallableTest::print, threadLocal1, threadLocal2),
                new ThreadLocalCallable<>(ThreadLocalCallableTest::print, threadLocal1, threadLocal3),
                new ThreadLocalCallable<>(ThreadLocalCallableTest::print, threadLocal2, threadLocal3),
                new ThreadLocalCallable<>(ThreadLocalCallableTest::print, threadLocal1, threadLocal2, threadLocal3)
        ));
        
        for(Future<String> future : futures){
            System.out.println(threadName + " : " + new Date()+ ":"+ future.get());
        }
        
        executor.shutdown();
        print();
    }
    
    private static String print() {
        String threadName = Thread.currentThread().getName();
        System.out.println(threadName + ":" + threadLocal1.get()
                + "   " + threadLocal2.get()
                + "   " + threadLocal3.get()
        );
        return threadName;
    }
}
