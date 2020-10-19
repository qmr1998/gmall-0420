package com.atguigu.gmall.cart.pojo;

import lombok.Data;

// ThreadLocal中的载荷对象UserInfo
@Data
public class UserInfo {

    private Long userId;
    private String userKey;
}