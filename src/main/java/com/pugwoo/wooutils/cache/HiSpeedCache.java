package com.pugwoo.wooutils.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 本地高速缓存
 * 1. 不需要依赖于redis，因此，不需要序列化和反序列化
 * 2. 因为是高速缓存，超时时间很短，同时为了避免缓存穿透，因此一律缓存null值
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HiSpeedCache {

    // 说明：对于本地高速缓存，使用包名+类名+方法名+方法名hashCode作为namespace，因此不需要用户指定namespace

    /**
     * [可选] 高速缓存的不同的key的mvel表达式脚本，可以从参数列表变量args中获取
     * @return 【重要】如果脚本执行出错，则打log，然后直接调用方法，等价于缓存失效。如果脚本直接结果返回null，则等价于空字符
     */
    String keyScript() default "";

    /**
     * 高速缓存的超时时间，默认1秒，建议使用1到10秒
     */
    int expireSecond() default 1;

    /**
     * 当缓存接口被访问时，自动设定后续自动刷新缓存的时间。缓存将以expireSecond的频率持续更新continueFetchSecond秒。
     */
    int continueFetchSecond() default 0;
}
