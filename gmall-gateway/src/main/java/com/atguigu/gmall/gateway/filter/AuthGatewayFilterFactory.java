package com.atguigu.gmall.gateway.filter;

import com.atguigu.gmall.common.utils.IpUtil;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.gateway.config.JwtProperties;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Lee
 * @date 2020-10-14  20:24
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.PathConfig> {

    @Autowired
    private JwtProperties jwtProperties;


    /**
     * 一定要重写构造方法
     * 告诉父类，这里使用PathConfig对象接收配置内容
     */
    public AuthGatewayFilterFactory() {
        super(PathConfig.class);
    }

    @Override
    public GatewayFilter apply(PathConfig config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                System.out.println("这是局部过滤器，只拦截特定服务！-----" + config);

                // Servlet: httpServletRequest --> webFlax: ServerHttpRequest
                ServerHttpRequest request = exchange.getRequest();
                ServerHttpResponse response = exchange.getResponse();

                // 1. 判断请求路径在不在拦截名单中，不在直接放行
                List<String> paths = config.paths; // 拦截名单
                String curPath = request.getURI().getPath(); // 当前请求的路径
                if(paths.stream().allMatch(path -> curPath.indexOf(path) == -1)){
                    return chain.filter(exchange);
                }

                // 2. 获取请求中的token。同步请求从cookie中获取，异步请求从header中获取
                // （走cookie太重，一个网站往往有很多cookie，如果通过携带cookie的方式传递token，网络传输压力太大）
                String token = request.getHeaders().getFirst("token");
                // 头信息没有，就从cookie中尝试获取
                if (StringUtils.isBlank(token)) {
                    MultiValueMap<String, HttpCookie> cookies = request.getCookies();
                    if (!CollectionUtils.isEmpty(cookies) && cookies.containsKey(jwtProperties.getCookieName())) {
                        HttpCookie cookie = cookies.getFirst(jwtProperties.getCookieName());
                        token = cookie.getValue();
                    }
                }

                // 3. 判断token是否为空。为空直接拦截
                if (StringUtils.isBlank(token)) {
                    // 重定向到登录页面
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                    // 请求结束
                    return response.setComplete();
                }

                try {
                    // 4. 如果不为空，解析jwt获取登录信息。解析异常直接拦截
                    Map<String, Object> map = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());

                    // 5. 判断是否被盗用。通过登录信息中的ip和当前请求的ip比较(获取载荷中ip，获取当前请求中的ip。如果两个ip不相等拦截)
                    String ip = map.get("ip").toString();
                    String curIp = IpUtil.getIpAddressAtGateway(request);
                    if (!StringUtils.equals(ip, curIp)) {
                        // 重定向到登录页面
                        response.setStatusCode(HttpStatus.SEE_OTHER);
                        response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                        // 请求结束
                        return response.setComplete();
                    }

                    // 6. 传递登录信息(载荷信息)给后续服务。后续各服务就不用再去解析了
                    request.mutate().header("userId", map.get("userId").toString()).build();
                    exchange.mutate().request(request).build();

                    // 7.放行
                    return chain.filter(exchange);
                } catch (Exception e) {
                    e.printStackTrace();
                    // 重定向到登录页面
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                    // 请求结束
                    return response.setComplete();
                }

            }
        };
    }

    /**
     * 指定读取字段的结果集类型
     * 默认通过map的方式，把配置读取到不同字段
     *  例如：/toLogin.html,/login
     *      由于只指定了一个字段，只能接收/toLogin.html
     * @return
     */
    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    /**
     * 指定字段顺序
     * 可以通过不同的字段分别读取：/toLogin.html,/login
     * 在这里希望通过一个集合字段读取所有的路径
     * @return
     */
    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("paths");
    }

    /**
     * 读取配置的内部类
     */
    @Data
    @ToString
    public static class PathConfig{
        private List<String> paths;
    }
}
