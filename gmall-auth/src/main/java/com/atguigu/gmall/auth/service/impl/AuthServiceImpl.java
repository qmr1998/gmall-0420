package com.atguigu.gmall.auth.service.impl;

import com.atguigu.gmall.auth.client.GmallUmsClient;
import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.service.AuthService;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.UserException;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtil;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lee
 * @date 2020-10-14  18:08
 */
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthServiceImpl implements AuthService {

    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public void login(String loginName, String password, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 1.远程调用ums查询用户接口
            ResponseVo<UserEntity> userEntityResponseVo = this.umsClient.queryUser(loginName, password);
            UserEntity userEntity = userEntityResponseVo.getData();

            // 2.对用户信息判空
            if (userEntity == null) {
                throw new UserException("用户名或者密码错误！");
            }

            // 3.组装载荷信息：userId,userName
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userEntity.getId());
            map.put("userName", userEntity.getUsername());

            // 4.防止被盗用，可以加入当前用户的ip
            String ip = IpUtil.getIpAddressAtService(request);
            map.put("ip", ip);

            // 5.生成jwt类型的token
            String token = JwtUtils.generateToken(map, this.jwtProperties.getPrivateKey(), this.jwtProperties.getExpire());

            // 6.把token放入cookie中
            CookieUtils.setCookie(request, response, this.jwtProperties.getCookieName(), token, this.jwtProperties.getExpire() * 60);

            // 7.为了登录成功之后显示用户昵称
            CookieUtils.setCookie(request, response, this.jwtProperties.getNickName(), userEntity.getNickname(), this.jwtProperties.getExpire() * 60);

        } catch (Exception e) {
            e.printStackTrace();
//            throw new UserException("用户名或者密码出错！");
        }
    }
}
