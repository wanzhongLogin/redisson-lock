package com.biz.primus.ms.base.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * 加锁工具
 * @author wan
 */
public interface DistributedLocker {

    /**
     * 获取锁对象
     * 注意:
     *      该方法只获取锁对象,并不会进行加锁操作
     */
    @Deprecated
    public RLock lock(String lockKey);

    /**
     * 带超时的锁
     * 超时时间尽量不要太长,太长了容易死锁
     */
    public RLock lock(String lockKey, int timeout);

    /**
     * 带超时的锁
     * @param lockKey
     * @param unit 时间类型,可以是秒,毫秒
     * @param timeout
     * @return
     */
    public RLock lock(String lockKey, TimeUnit unit, int timeout);

    /**
     * 解锁,可以直接来key
     */
    public void unlock(String lockKey);

    /**
     * 解锁,也可以直接传加锁返回的参数
     */
    public void unlock(RLock lock);


    public void setRedissonClient(RedissonClient redissonClient);
}
