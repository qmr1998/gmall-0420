package com.atguigu.gmall.auth.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Lee
 * @date 2020-10-14  18:08
 */
public interface AuthService {
    void login(String loginName, String password, HttpServletRequest request, HttpServletResponse response);
}
