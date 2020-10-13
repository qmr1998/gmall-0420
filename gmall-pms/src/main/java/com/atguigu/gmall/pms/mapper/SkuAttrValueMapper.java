package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 * 
 * @author qmr
 * @email qmr@atguigu.com
 * @date 2020-09-21 18:48:44
 */
@Mapper
@Component
public interface SkuAttrValueMapper extends BaseMapper<SkuAttrValueEntity> {

    public List<SkuAttrValueEntity> querySkuAttrValuesBySpuId(Long spuId);

    public List<Map<String, Object>> querySkuIdMappingSaleAttrValueBySpuId(Long spuId);

}
