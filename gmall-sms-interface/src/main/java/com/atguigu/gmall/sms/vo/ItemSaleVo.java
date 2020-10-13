package com.atguigu.gmall.sms.vo;

import lombok.Data;

/**
 * @author Lee
 * @date 2020-10-12  18:05
 */
// 营销信息
@Data
public class ItemSaleVo {

    private String type; // 类型：积分 满减 打折
    private String desc; // 描述信息

}
