package com.atguigu.gmall.pms.vo;

import lombok.Data;

/**
 * @author Lee
 * @date 2020-10-12  18:07
 */
// 规格参数
@Data
public class AttrValueVo {

    private Long attrId; // 参数id
    private String attrName; // 参数名
    private String attrValue; // 参数值

}
