package com.eatguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Lee
 * @date 2020-10-12  17:58
 */
// 商品详情页总数据模型
@Data
public class ItemVo {

    // 包含一、二、三级分类元素
    private List<CategoryEntity> categories;

    // 品牌
    private Long brandId;
    private String brandName;

    // spu信息
    private Long spuId;
    private String spuName;

    // sku信息
    private Long skuId;
    private String title;
    private String subTitle;
    private BigDecimal price;
    private String defaultImage;
    private Integer weight;

    // sku图片
    private List<SkuImagesEntity> images;

    // 营销信息
    private List<ItemSaleVo> sales;

    // 库存信息(是否有货，默认为false)
    private Boolean store = false;

    // sku所属spu下的所有sku的销售属性
    // [{attrId: 8, attrName: '颜色', attrValues: ['白色', '黑色']},
    // {attrId: 9, attrName: '内存', attrValues: ['8G', '12G']},
    // {attrId: 10, attrName: '存储', attrValues: ['128G', '256G', '512G']}]
    private List<SaleAttrValueVo> saleAttrs;

    // 当前sku的销售属性： {8: '白色'，9: '8G', 10: '512G'}
    private Map<Long, String> saleAttr;

    // 销售属性组合和skuId映射关系
    // {'白色, 8G, 128G': 10, '黑色, 8G, 128G': 11}
    private String skuJsons;

    // 商品描述(spu的海报信息)
    private List<String> spuImages;

    // 规格参数组及组下的规格参数(带值)
    private List<ItemGroupVo> groups;

}
