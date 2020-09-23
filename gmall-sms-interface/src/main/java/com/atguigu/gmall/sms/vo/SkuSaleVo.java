package com.atguigu.gmall.sms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Lee
 * @date 2020-09-23  18:23
 */
@Data
public class SkuSaleVo {

    private Long skuId;

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
}
