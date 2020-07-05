package com.pugwoo.wooutils.thread;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 锟斤拷 <br>
 * 约定! ThreadLocal实例必须为静态
 */
public class ThreadLocalContent {
    
    /**
     * 通用的ThreadLocal集合，每次都会处理这些
     */
    private static final Set<ThreadLocal> COMMON_THREAD_LOCAL_SET = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
    /**
     * 获取通用的threadLocal集合，新的实例，调用者修改不会影响该值
     * @return 通用的threadLocal集合
     */
    public static Set<ThreadLocal> getCommonThreadLocal() {
        return new HashSet<>(COMMON_THREAD_LOCAL_SET);
    }
    
    /**
     * 添加通用的threadLocal
     */
    public static ThreadLocalContent addCommonThreadLocal(ThreadLocal threadLocal) {
        if (threadLocal != null && !COMMON_THREAD_LOCAL_SET.contains(threadLocal)) {
            COMMON_THREAD_LOCAL_SET.add(threadLocal);
        }
        return null;
    }
    
    /**
     * 删除通用的threadLocal
     */
    public static ThreadLocalContent removeCommonThreadLocal(ThreadLocal threadLocal) {
        if (threadLocal != null && COMMON_THREAD_LOCAL_SET.contains(threadLocal)) {
            COMMON_THREAD_LOCAL_SET.remove(threadLocal);
        }
        return null;
    }
    
    /**
     * 清空通用的threadLocal
     */
    public static ThreadLocalContent clearCommonThreadLocal() {
        COMMON_THREAD_LOCAL_SET.clear();
        return null;
    }
    
    /**
     * 将valueMap的对应关系的值设置到threadLocal中
     *   子线程运行前调用
     */
    static void setValueToThreadLocal(Map<ThreadLocal, Object> valueMap) {
        if (valueMap != null) {
            for (Map.Entry<ThreadLocal, Object> entry : valueMap.entrySet()) {
                entry.getKey().set(entry.getValue());
            }
        }
    }
    
    /**
     * 将valueMap的对应关系的threadLocal值清空
     *   子线程finally中执行
     */
    static void removeValueInThreadLocal(Map<ThreadLocal, Object> valueMap) {
        if (valueMap != null) {
            for (ThreadLocal threadLocal : valueMap.keySet()) {
                threadLocal.remove();
            }
        }
    }
    
    /**
     * 获取 threadLocal -> value 对应关系
     *   主线程新建ThreadLocalCallable ThreadLocalRunnable时调用
     */
    static Map<ThreadLocal, Object> getValueMap(ThreadLocal... threadLocals) {
        Map<ThreadLocal, Object> valueMapTemp = new HashMap<>();
        for (ThreadLocal threadLocal : COMMON_THREAD_LOCAL_SET) {
            if (threadLocal != null && !valueMapTemp.containsKey(threadLocal)) {
                valueMapTemp.put(threadLocal, threadLocal.get());
            }
        }
        if (threadLocals != null) {
            for (ThreadLocal threadLocal : threadLocals) {
                if (threadLocal != null && !valueMapTemp.containsKey(threadLocal)) {
                    valueMapTemp.put(threadLocal, threadLocal.get());
                }
            }
        }
        return valueMapTemp.isEmpty() ? null : valueMapTemp;
    }
}
