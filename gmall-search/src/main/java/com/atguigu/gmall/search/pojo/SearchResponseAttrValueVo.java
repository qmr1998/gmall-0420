package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author Lee
 * @date 2020-09-28  16:42
 */
@Data
public class SearchResponseAttrValueVo {

    // 属性id
    private Long attrId;

    // 属性名
    private String attrName;

    // 属性值
    private List<String> attrValues;
}
