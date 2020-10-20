package com.atguigu.gmall.order.vo;

import lombok.Data;

// ThreadLocal中的载荷对象UserInfo
@Data
public class UserInfo {

    private Long userId;
    private String userKey;
}