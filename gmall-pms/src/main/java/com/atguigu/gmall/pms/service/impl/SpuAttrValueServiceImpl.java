package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import org.springframework.util.CollectionUtils;


@Service("spuAttrValueService")
public class SpuAttrValueServiceImpl extends ServiceImpl<SpuAttrValueMapper, SpuAttrValueEntity> implements SpuAttrValueService {

    @Autowired
    private AttrMapper attrMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    // 搜索功能之根据categoryId和spuId查询SpuAttrValueEntity，主要需要attrId,attrName,attrValue
    @Override
    public List<SpuAttrValueEntity> querySearchSpuAttrValuesByCidAndSpuId(Long cid, Long spuId) {
        // 1、根据 categoryId 查询出检索类型的规格参数
        List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("category_id", cid).eq("search_type", 1));
        if (CollectionUtils.isEmpty(attrEntities)) {
            return null;
        }
        // 若attrEntities不为空，就获取 检索规格参数id
        List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());

        // 2、根据spuId和attrIds查询 销售检索类型规格参数和值
        List<SpuAttrValueEntity> spuAttrValueEntities = baseMapper.selectList(new QueryWrapper<SpuAttrValueEntity>().eq("spu_id", spuId).in("attr_id", attrIds));
        return spuAttrValueEntities;
    }

}