package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Lee
 * @date 2020-10-11  11:06
 */
@Aspect
@Component
public class GmallCacheAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RBloomFilter bloomFilter;

    /**
     * api回顾：
     * 获取目标方法参数：joinPoint.getArgs()
     * 获取目标方法所在类：joinPoint.getTarget().getClass()
     * 获取目标方法签名：(MethodSignature)joinPoint.getSignature()
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("@annotation(GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        // 布隆过滤器解决缓存穿透，先用布隆过滤器来判断，如果不存在，连缓存都不用查了，直接return null
        // TODO: 将布隆过滤器放入单独的前置通知方法中
        List<Object> args =  Arrays.asList(joinPoint.getArgs()); // 获取目标方法的参数
        String pid = args.get(0).toString();
        if(!this.bloomFilter.contains(pid)){
            return null;
        }

        // 获取切点方法的签名对象
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取目标方法对象
        Method method = signature.getMethod();
        // 获取方法上指定注解的对象
        GmallCache gmallCache = method.getAnnotation(GmallCache.class);
        // 获取目标方法的返回值类型
        Class<?> returnType = method.getReturnType();
        // 获取目标方法的参数（置于最上方了）
//        List<Object> args = Arrays.asList(joinPoint.getArgs());

        // --获取缓存中的前缀
        String prefix = gmallCache.prefix();
        // key = KEY_PREFIX + parentId,即 prefix + args(方法参数)
        String key = prefix + args;

        // 拦截前代码块：判断缓存中有没有
        String json = this.redisTemplate.opsForValue().get(key);
        // 判断缓存中的数据是否为空
        if (StringUtils.isNotBlank(json)) {
            // 缓存中有直接返回
            return JSON.parseObject(json, returnType);
        }

        // 缓存中没有，加分布式锁
        String lock = gmallCache.lock() + args; // 锁名 = lock + args
        RLock fairLock = this.redissonClient.getFairLock(lock);
        fairLock.lock();
        Object result;

        try {
            // 再去查询缓存，缓存中有直接返回(加锁的过程中，别的请求可能已经把数据放入缓存)
            String json2 = this.redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotBlank(json2)) {
                fairLock.unlock();
                return JSON.parseObject(json2, returnType);
            }

            // 执行目标方法
            result = joinPoint.proceed(joinPoint.getArgs());

            // 拦截后代码块：放入缓存 释放分布锁
            // 缓存时间：设置的过期时间 timeout + 防止雪崩的随机时间 random
            int timeout = gmallCache.timeout() + new Random().nextInt(gmallCache.random());
            this.redisTemplate.opsForValue().set(key, JSON.toJSONString(result), timeout, TimeUnit.MINUTES);
        } finally {
            fairLock.unlock();
        }

        return result;
    }


}
