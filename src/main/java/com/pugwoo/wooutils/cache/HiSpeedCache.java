package com.pugwoo.wooutils.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 本地高速缓存
 * 1. 可以不依赖于redis，因此，不需要序列化和反序列化。但是要【特别注意】缓存的值为Java的对象引用，也即返回值的使用者，如果修改了返回值，将等于直接修改了缓存的值，存在bug风险。此时可以开启cloneReturn
 * 2. 因为是高速缓存，超时时间很短，同时为了避免缓存穿透，因此一律缓存null值
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HiSpeedCache {

    // 说明：对于本地高速缓存，使用包名+类名+方法名+方法名hashCode作为namespace，因此不需要用户指定namespace

    /**
     * [可选] 高速缓存的不同的key的mvel表达式脚本，可以从参数列表变量args中获取<br>
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

    /**
     * 是否json克隆返回数据，默认false。<br>
     * 如果启动克隆，那么调用者对返回值进行修改，就不会影响缓存的值。<br>
     * 【注意】当此值为true时，请自行测试验证克隆的数据是否有问题。<br>
     */
    boolean cloneReturn() default false;

    /**
     * 如果克隆的数据是泛型的，则这里支持指定泛型，这个是第1个泛型
     */
    Class<?> genericClass1() default Void.class;

    /**
     * 如果克隆的数据是泛型的，则这里支持指定泛型，这个是第2个泛型
     */
    Class<?> genericClass2() default Void.class;

    /**
     * 是否使用redis保存数据，默认关闭。<br>
     * 只有当前是Spring容器且有RedisHelper的bean时，useRedis=true才生效，否则等价于useRedis=false，即便设置为true。<br>
     * 当使用useRedis=true时，cloneReturn选项失效。<br>
     * 【注意】当返回类型有泛型时，记得设置genericClass1或genericClass2的值，最多支持2个泛型的情况。<br>
     */
    boolean useRedis() default false;

}
