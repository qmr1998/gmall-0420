package com.atguigu.gmall.search.pojo;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * @author Lee
 * @date 2020-09-27  16:38
 * <p>
 * 搜索条件中的规格参数，包含id、name、value
 */
@Data
public class SearchAttrValueVo {

    @Field(type = FieldType.Long)
    private Long attrId;
    @Field(type = FieldType.Keyword)
    private String attrName;
    @Field(type = FieldType.Keyword)
    private String attrValue;

}
