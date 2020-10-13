package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.Set;

/**
 * @author Lee
 * @date 2020-10-12  18:06
 */
// sku的销售属性
@Data
public class SaleAttrValueVo {

    private Long attrId; // 属性id
    private String attrName; // 属性名
    private Set<String> attrValues; // 属性值

}
