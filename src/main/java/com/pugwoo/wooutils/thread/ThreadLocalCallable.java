package com.pugwoo.wooutils.thread;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author 锟斤拷 <br/>
 * @date 2020/07/04 <br/>
 * 适用于将指定的threadLocal的值从父线程传递到子线程中
 *
 *   {@link Thread#inheritableThreadLocals} 有类似的功能，但是其在线程创建的时候进行值传递初始化工作，使用线程池时会有点小问题
 */
public class ThreadLocalCallable<V> implements Callable<V> {
    
    /** Callable */
    private final Callable<V> callable;
    
    /** 存储threadLocal对应的值，会在子线程运行时设置进去 */
    private Map<ThreadLocal, Object> valueMap;
    
    /**
     * @param callable           Callable实例
     * @param staticThreadLocals ThreadLocal静态实例列表，禁止传非静态实例进来;
     *                           如有通用的不需要每次都传递参数，可以添加到公共的列表中;
     *                           {@link ThreadLocalContent#COMMON_THREAD_LOCAL_SET}
     */
    public ThreadLocalCallable(Callable<V> callable, ThreadLocal... staticThreadLocals) {
        this.valueMap = ThreadLocalContent.getValueMap(staticThreadLocals);
        this.callable = callable;
    }
    
    @Override
    public V call() throws Exception {
        try {
            ThreadLocalContent.setValueToThreadLocal(valueMap);
            return callable.call();
        } finally {
            ThreadLocalContent.removeValueInThreadLocal(valueMap);
        }
    }
}
