package com.biz.primus.ms.base.lock;

import com.biz.primus.base.exception.BizSilentException;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * 支持注解类型的加锁
 * @author wan
 */
@Aspect
@Configuration
@Component
public class DistributedLockAspect {

    @Autowired
    private LockService lockService;

    @Pointcut("@annotation(com.biz.primus.ms.base.lock.DistributedLock)")
    public void DistributedLockAspect() {

    }

    /**
     * 环绕通知lockUtils
     */
    @Around(value = "DistributedLockAspect()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        Class targetClass = pjp.getTarget().getClass();
        String methodName = pjp.getSignature().getName();
        Class[] parameterTypes = ((MethodSignature) pjp.getSignature()).getMethod().getParameterTypes();
        Method method = targetClass.getMethod(methodName, parameterTypes);
        final String lockName = getLockName(targetClass,method);
        return lock(pjp, method, lockName);
    }


    @AfterThrowing(value = "DistributedLockAspect()", throwing = "ex")
    public void afterThrowing(Throwable ex) {
        throw new BizSilentException(ex.getMessage());
    }

    /**
     * 获取加锁的方法名
     */
    String getLockName(Class clazz,Method method) {

        //不能为空
        Objects.requireNonNull(method);

        //获取加的注解
        DistributedLock annotation = method.getAnnotation(DistributedLock.class);

        String lockName = annotation.lockName();
        if(StringUtils.isEmpty(lockName)){
            StringBuffer sb = new StringBuffer();
            lockName = sb.append(clazz.getName()).append(method.getName()).toString();
        }
        return lockName;
    }

    Object lock(ProceedingJoinPoint pjp, Method method, final String lockName) {
        DistributedLock annotation = method.getAnnotation(DistributedLock.class);
        boolean tryLock = annotation.tryLock();
        if (tryLock) {
            return tryLock(pjp, annotation, lockName);
        } else {
            return lock(pjp, lockName);
        }
    }

    Object tryLock(ProceedingJoinPoint pjp, DistributedLock annotation, final String lockName) {
        int waitTime = annotation.waitTime();
        int leaseTime = annotation.leaseTime();
        TimeUnit timeUnit = annotation.timeUnit();
        RLock lock = lockService.lock(lockName);
        try {
            boolean tryLock = lock.tryLock(waitTime,leaseTime,timeUnit);
            if(tryLock){
                return proceed(pjp);
            }else{
                throw new BizSilentException("获取锁资源失败");
            }
        } catch (Exception e){
            throw new BizSilentException("获取锁资源失败["+ e.getMessage() +"]");
        }finally {
            lockService.unlock(lockName);
        }
    }

    Object lock(ProceedingJoinPoint pjp, final String lockName) {
        RLock lock = lockService.lock(lockName);
        try {
            lock.lock();
            return proceed(pjp);
        } finally {
            lockService.unlock(lockName);
        }
    }

    Object proceed(ProceedingJoinPoint pjp) {
        try {
            return pjp.proceed();
        } catch (Throwable throwable) {
            throw new BizSilentException(throwable.getMessage());
        }
    }
}
