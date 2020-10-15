package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @author Lee
 * @date 2020-09-21  22:49
 */
@Configuration
public class CorsFilterConfig {

    @Bean
    public CorsWebFilter corsWebFilter(){
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // 允许跨域访问的域名，为了方便将来携带cookie，这里不使用*号（*号代表允许所有域名跨域访问，若设置为*就不能设置携带cookie了）
        corsConfiguration.addAllowedOrigin("http://manager.gmall.com");
        corsConfiguration.addAllowedOrigin("http://mall.com");
        corsConfiguration.addAllowedOrigin("http://www.gmall.com");
        corsConfiguration.addAllowedOrigin("http://index.gmall.com");
        // 允许携带cookie信息
        corsConfiguration.setAllowCredentials(true);
        // 允许所有请求方式跨域访问
        corsConfiguration.addAllowedMethod("*");
        // 允许携带所有头信息跨域访问
        corsConfiguration.addAllowedHeader("*");


        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        //  /**表示对经过网关的所有路径进行跨域校验
        configurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsWebFilter(configurationSource);

    }

}
