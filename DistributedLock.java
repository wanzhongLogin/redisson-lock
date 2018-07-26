package com.biz.primus.ms.base.lock;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 锁注解,对方法起效果
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    /**
     * 锁的名称。
     * 如果lockName可以确定，直接设置该属性。
     */
    String lockName() default "";

    /**
     * 是否使用尝试锁。
     */
    boolean tryLock() default false;
    /**
     * 最长等待时间。
     * 该字段只有当tryLock()返回true才有效。
     */
    int waitTime() default 3000;
    /**
     * 锁超时时间。
     * 超时时间过后，锁自动释放。
     */
    int leaseTime() default 10;
    /**
     * 时间单位。默认为秒。
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

}