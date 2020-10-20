package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Lee
 * @date 2020-10-19  17:56
 */
@Data
public class OrderItemVo {
    private Long skuId;
    private String defaultImage;
    private String title;
    private List<SkuAttrValueEntity> skuAttrs; // 销售属性：List<SkuAttrValueEntity>的json格式
    private BigDecimal price; // 加入购物车时的价格
    private BigDecimal count; // 购物车商品数量
    private Boolean store = false; //是否有货
    private List<ItemSaleVo> sales; // 销售属性：List<SkuAttrValueEntity>的json格式
    private BigDecimal weight;
}
