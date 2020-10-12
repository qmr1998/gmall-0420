package com.atguigu.gmall.index.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author Lee
 * @date 2020-10-10  21:25
 */
@Component
public class DistributedLock {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Thread thread;

    // 获取锁
    public Boolean tryLock(String lockName, String uuid, Long expireTime) {
        String script =
                "if (redis.call('exists', KEYS[1]) == 0 or redis.call('hexists', KEYS[1], ARGV[1]) == 1) " +
                "then " +
                    "redis.call('hincrby', KEYS[1], ARGV[1], 1); " +
                    "redis.call('expire', KEYS[1], ARGV[2]); " +
                    "return 1; " +
                "else " +
                    "return 0; " +
                "end";
        if (!this.redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuid, expireTime.toString())) {
            try {
                Thread.sleep(30);
                // 没有获取到锁，重试(递归调用自己)
                tryLock(lockName, uuid, expireTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // 加锁成功就开启自动续期
        this.renewExpire(lockName, uuid, expireTime);
        // 获取到锁，返回true
        return true;
    }

    // 释放锁
    public void unlock(String lockName, String uuid) {
        String script =
                "if (redis.call('hexists', KEYS[1], ARGV[1]) == 0) " +
                "then " +
                    "return nil; " +
                "elseif (redis.call('hincrby', KEYS[1], ARGV[1], -1) > 0) " +
                "then " +
                    "return 0; " +
                "else " +
                    "redis.call('del', KEYS[1]) " +
                    "return 1; " +
                "end";
        // 这里之所以没有跟加锁一样使用 Boolean ,这是因为解锁 lua 脚本中，三个返回值含义如下：
        // 1 代表解锁成功，锁被释放
        // 0 代表可重入次数被减 1
        // null 代表其他线程尝试解锁，解锁失败
        Long result = this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(lockName), uuid);
        // 释放锁的时候结束掉自动续期的线程
        thread.interrupt();
        if (result == null) {
            throw new RuntimeException("attempt to unlock lock, not locked by lockName: " + lockName + " with request: " + uuid);
        }

    }

    /**
     * 锁延期
     * 线程等待超时时间的2/3时间后,执行锁延时代码,直到业务逻辑执行完毕,因此在此过程中,其他线程无法获取到锁,保证了线程安全性
     * 一旦加锁成功，就自动续期
     * 释放锁的时候结束该线程
     * @param lockName
     * @param expireTime 单位：毫秒
     */
    public void renewExpire(String lockName, String uuid, Long expireTime){
        String script =
                "if (redis.call('hexists', KEYS[1], ARGV[1]) == 1) " +
                "then " +
                    "redis.call('expire', KEYS[1], ARGV[2]); " +
                    "return 1; " +
                "else " +
                    "return 0; " +
                "end";
        thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(expireTime * 1000 * 2 / 3);
                    // 自动续期
                    this.redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuid, expireTime.toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "");
        thread.start();

    }
}
