package com.atguigu.gmall.cart.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author Lee
 * @date 2020-10-16  20:35
 */
@Component
@Slf4j
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY = "cart:async:exception";

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        log.error("异步调用方法出现异常，方法：{}，参数：{}，异常信息：{}", method, params, ex.getMessage());

        // 将错误的保存到redis中
        BoundListOperations<String, String> listOps = this.redisTemplate.boundListOps(KEY);
        listOps.leftPush(params[0].toString());

    }
}
