package com.atguigu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.vo.AttrValueVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import org.springframework.util.CollectionUtils;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {

    @Autowired
    private AttrMapper attrMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    // 搜索功能之根据categoryId和skuId查询SkuAttrValueEntity，主要需要attrId,attrName,attrValue
    @Override
    public List<SkuAttrValueEntity> querySearchSkuAttrValuesByCidAndSkuId(Long cid, Long skuId) {
        // 1、根据 categoryId 查询出检索类型的规格参数
        List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("category_id", cid).eq("search_type", 1));
        if (CollectionUtils.isEmpty(attrEntities)) {
            return null;
        }
        // 若attrEntities不为空，就获取 检索规格参数id
        List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());

        // 2、根据skuId和attrIds查询 销售检索类型规格参数和值
        List<SkuAttrValueEntity> skuAttrValueEntities = baseMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id", skuId).in("attr_id", attrIds));
        return skuAttrValueEntities;
    }

    // 根据spuId查询spu下的所有销售属性
    @Override
    public List<SaleAttrValueVo> querySkuAttrValuesBySpuId(Long spuId) {
        List<SkuAttrValueEntity> skuAttrValueEntities = this.skuAttrValueMapper.querySkuAttrValuesBySpuId(spuId);
        if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
            Map<Long, List<SkuAttrValueEntity>> map = skuAttrValueEntities.stream().collect(Collectors.groupingBy(SkuAttrValueEntity::getAttrId));

            List<SaleAttrValueVo> saleAttrValueVos = new ArrayList<>();

            map.forEach((attrId, attrValueEntities) -> {
                SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
                saleAttrValueVo.setAttrId(attrId);
                saleAttrValueVo.setAttrName(attrValueEntities.get(0).getAttrName());
                saleAttrValueVo.setAttrValues(attrValueEntities.stream().map(SkuAttrValueEntity::getAttrValue).collect(Collectors.toSet()));

                saleAttrValueVos.add(saleAttrValueVo);
            });
            return saleAttrValueVos;
        }

        return null;
    }

    // 根据skuId查询sku的销售属性
    @Override
    public List<SkuAttrValueEntity> querySkuAttrValueBySkuId(Long skuId) {
        List<SkuAttrValueEntity> skuAttrValueEntities = this.baseMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id", skuId));
        return skuAttrValueEntities;
    }

    @Override
    public String querySkuIdMappingSaleAttrValueBySpuId(Long spuId) {

        // [{"sku_id": 3, "attr_values": "暗夜黑,12G,512G"}, {"sku_id": 4, "attr_values": "白天白,12G,512G"}]
        List<Map<String, Object>> maps = this.skuAttrValueMapper.querySkuIdMappingSaleAttrValueBySpuId(spuId);

        // 转换成：{'暗夜黑,12G,512G': 3, '白天白,12G,512G': 4}
        Map<Object, Object> jsonMap = maps.stream().collect(Collectors.toMap(map -> map.get("attr_values").toString(), map -> (Long) map.get("sku_id")));

        return JSON.toJSONString(jsonMap);
    }


}