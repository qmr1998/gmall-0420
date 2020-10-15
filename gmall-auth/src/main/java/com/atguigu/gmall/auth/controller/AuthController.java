package com.atguigu.gmall.auth.controller;

import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Lee
 * @date 2020-10-14  18:07
 */
@Controller
public class AuthController {

    // 登录流程
//    -1 客户端携带用户名和密码请求登录 ，并携带登录前页面的路径
//    -2 授权中心调用用户中心接口，根据用户名和密码查询用户信息
//    -3 用户名密码不正确，不能获取用户，登录失败
//    -4 如果校验成功，则生成JWT，jwt要防止别人盗取
//    -5 把jwt放入cookie
//    -6 为了方便页面展示登录用户昵称，向cookie中单独写入昵称（例如京东cookie中的的**unick**）
//    -7 重定向 回到登录前的页面

    @Autowired
    private AuthService authService;

    @GetMapping("toLogin.html")
    public String toLogin(@RequestParam(value = "returnUrl", required = false) String returnUrl, Model model) {
        // 把登录前的页面地址，记录到登录页面，以备将来登录成功，回到登录前的页面
        model.addAttribute("returnUrl", returnUrl);
        return "login";
    }

    @PostMapping("login")
    public String login(
            @RequestParam("loginName") String loginName,
            @RequestParam("password") String password,
            @RequestParam(value = "returnUrl", required = false) String returnUrl,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        this.authService.login(loginName, password, request, response);
        // 登录后跳转到之前的页面
        return "redirect:" + returnUrl;
    }
}
