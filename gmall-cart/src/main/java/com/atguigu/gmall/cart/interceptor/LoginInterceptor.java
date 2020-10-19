package com.atguigu.gmall.cart.interceptor;

import com.atguigu.gmall.cart.config.JwtProperties;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

/**
 * @author Lee
 * @date 2020-10-16  11:50
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    // 声明线程的局部变量
    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 在购物车微服务中统一获取用户的登录状态：userId userKey
        UserInfo userInfo = new UserInfo();

        // 1.获取cookie中的Token 以及 userKey
        String userKey = CookieUtils.getCookieValue(request, this.jwtProperties.getUserKey());
        // 如果没有userKey，就为其设置一个uuid
        if (StringUtils.isBlank(userKey)) {
            userKey = UUID.randomUUID().toString();
            CookieUtils.setCookie(request, response, this.jwtProperties.getUserKey(), userKey, this.jwtProperties.getExpire());
        }
        userInfo.setUserKey(userKey);

        // 2.判断token是否为空，如果为空，直接传递userKey即可
        String token = CookieUtils.getCookieValue(request, this.jwtProperties.getCookieName());
        if (StringUtils.isBlank(token)) {
            THREAD_LOCAL.set(userInfo);
            return true;
        }

        try {
            // 如果token不为空，解析jwt类型的token获取userId传递给后续业务
            Map<String, Object> map = JwtUtils.getInfoFromToken(token, this.jwtProperties.getPublicKey());
            userInfo.setUserId(Long.valueOf(map.get("userId").toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        THREAD_LOCAL.set(userInfo);

        // 购物车拦截器中不管有没有登录都要放行，所以这里直接return true
        return true;
    }

    /**
     * 封装了一个获取线程局部变量值的静态方法
     * @return
     */
    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();
    }

    /**
     * 在视图渲染完成之后执行，经常在完成方法中释放资源
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 调用删除方法，是必须选项。因为使用的是tomcat线程池，请求结束后，线程不会结束。
        // 如果不手动删除线程变量，可能会导致内存泄漏
        THREAD_LOCAL.remove();
    }
}
