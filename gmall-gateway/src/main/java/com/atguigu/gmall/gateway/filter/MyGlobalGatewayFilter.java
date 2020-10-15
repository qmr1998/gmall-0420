package com.atguigu.gmall.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author Lee
 * @date 2020-10-14  19:37
 */
@Component
public class MyGlobalGatewayFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("这是全局过滤器，它会无差别拦截所有经过网关的请求。。。");

        // 拦截

        // 放行
        return chain.filter(exchange);
    }

    /**
     * 指定全局过滤器的优先级，返回值越小，优先级越高
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
