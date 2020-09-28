package com.atguigu.gmall.search.pojo;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

/**
 * @author Lee
 * @date 2020-09-28  16:38
 */
@Data
public class SearchResponseVo {

    // 封装品牌过滤条件
    private List<BrandEntity> brands;

    // 封装分类过滤条件
    private List<CategoryEntity> categories;

    // 封装规格参数过滤条件 [{attrId:4, attrName:运行内存, attrValues:[8G, 12G]},{attrId:5, attrName:机身存储, attrValues:[128G, 256G]}]
    private List<SearchResponseAttrValueVo> filters;

    // 分页数据
    private Integer pageNum;
    private Integer pageSize;
    private Long total; // 总记录数
    private List<Goods> goodsList; // 当前页记录
}
