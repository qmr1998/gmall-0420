package com.atguigu.gmall.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Lee
 * @date 2020-10-13  11:53
 */
@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(
            @Value("${threadPool.corePoolSize}")Integer corePoolSize,
            @Value("${threadPool.maximumPoolSize}")Integer maximumPoolSize,
            @Value("${threadPool.keepAliveTime}")Integer keepAliveTime,
            @Value("${threadPool.blockingQueueCapacity}")Integer blockingQueueCapacity
    ) {
        return new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(blockingQueueCapacity),
                Executors.defaultThreadFactory(),
                (Runnable r, ThreadPoolExecutor executor) -> {
                    // 记录被拒绝的请求
                    System.out.println("您的请求被拒绝了");
                });
    }
}
