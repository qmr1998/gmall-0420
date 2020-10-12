package com.atguigu.gmall.index.aspect;

import java.lang.annotation.*;

/**
 * @author Lee
 * @date 2020-10-11  10:53
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {

    /**
     * 缓存key的前缀
     * @return
     */
    String prefix() default "";

    /**
     * 设置缓存的有效时间
     * 单位：分钟
     * @return
     */
    int timeout() default 5;

    /**
     * 为了避免缓存雪崩，给缓存时间添加随机值：单位分钟
     * @return
     */
    int random() default 5;

    /**
     * 防止击穿，分布式锁的key
     * @return
     */
    String lock() default "lock:";
}
