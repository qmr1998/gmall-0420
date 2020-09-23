package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Lee
 * @date 2020-09-23  12:47
 */
@Data
public class SkuVo extends SkuEntity {

    // skus一部分字段对应的是pms_sku这张表，对应的实体类是SkuEntity。
    // 所以让其继承 SkuEntity，images这个字段保存到pms_sku_images这张表。
    // 一部分字段对应的是sms_sku_bounds表，对应的实体类是gmall-sms服务中的SkuBoundsEntity
    // 一部分字段对应的表是sms_sku_full_reduction，对应的实体类是gmall-sms微服务中的SkuFullReductionEntity
    // 一部分字段对应的表是sms_sku_ladder，对应的实体类是gmall-sms微服务的SkuLadderEntity
    // 最后一个字段就是saleAttrs，对应的是pms_sku_attr_value，实体类是`List<SkuAttrValueEntity>`
    // 由于java是单继承，所以这里只能选择一个继承，其他的字段在从对应的实体类中copy过来。这里选择扩展SkuEntity这个实体类.

    // 图片
    private List<String> images;

    // 积分相关字段（成长积分，购物积分，优惠生效情况）
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private List<Integer> work; // 注意这里是个集合，因为优惠生效情况有四个选项

    // 满减相关字段（满多少钱减多少钱，是否叠加其他优惠）
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;

    // 打折相关字段（满几件打几折，是否叠加其他优惠）
    private Integer fullCount;
    private BigDecimal discount;
    private Integer ladderAddOther;

    // saleAttrs:销售属性SkuEntity
    private List<SkuAttrValueEntity> saleAttrs;

}
