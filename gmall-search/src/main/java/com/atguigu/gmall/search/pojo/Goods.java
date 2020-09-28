package com.atguigu.gmall.search.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

/**
 * @author Lee
 * @date 2020-09-27  13:56
 */
@Data
@Document(indexName = "goods", type = "info", shards = 3, replicas = 2)
public class Goods {

    // 商品搜索列表字段
    @Id
    @Field(type = FieldType.Long)
    private Long skuId;
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;
    @Field(type = FieldType.Keyword, index = false)
    private String subTitle;
    @Field(type = FieldType.Keyword, index = false)
    private String defaultImage;
    @Field(type = FieldType.Double)
    private Double price;

    // 品牌聚合字段
    @Field(type = FieldType.Long)
    private Long brandId;
    @Field(type = FieldType.Keyword)
    private String brandName;
    @Field(type = FieldType.Keyword, index = false)
    private String logo;

    // 分类聚合字段
    @Field(type = FieldType.Long)
    private Long categoryId;
    @Field(type = FieldType.Keyword)
    private String categoryName;

    // 规格参数字段(包含规格参数的id，name，value)
    @Field(type = FieldType.Nested)
    private List<SearchAttrValueVo> searchAttrs;

    // 排序和筛选字段
    @Field(type = FieldType.Long)
    private Long sales; // 销量
    @Field(type = FieldType.Date)
    private Date createTime; // 新品
    @Field(type = FieldType.Boolean)
    private Boolean store = false; // 是否有货，默认无货


}
