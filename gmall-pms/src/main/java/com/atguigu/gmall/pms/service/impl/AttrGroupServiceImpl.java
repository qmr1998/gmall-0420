package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.vo.AttrValueVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import org.springframework.beans.BeanUtils;
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

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrMapper attrMapper;

    @Autowired
    private SpuAttrValueMapper spuAttrValueMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    // 根据catId查询分类下的组及规格参数
    @Override
    public List<AttrGroupEntity> queryGroupsWithAttrsByCid(Long cid) {
        // 分开查询，先查询分组，再查询每个分组下的规格参数
        List<AttrGroupEntity> attrGroupEntities = baseMapper.selectList(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));

        // 如果查询出的组为空，直接返回null即可
        if (CollectionUtils.isEmpty(attrGroupEntities)) {
            return null;
        }

        // 遍历组，查询每个组的规格参数
        attrGroupEntities.forEach(attrGroupEntity -> {
            // 注意只查询基本参数，不要连销售参数一并查询过来
            List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", attrGroupEntity.getId()).eq("type", 1));
            // 将查询出的规格参数设置给attrGroupEntity对象
            attrGroupEntity.setAttrEntities(attrEntities);
        });

        return attrGroupEntities;
    }

    // 查询组及组下参数和值
    @Override
    public List<ItemGroupVo> queryGroupsBySpuIdAndSkuIdAndCid(Long spuId, Long skuId, Long cid) {

        // 1.根据cid查询规格参数分组
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));
        if (CollectionUtils.isEmpty(attrGroupEntities)) {
            return null;
        }

        return attrGroupEntities.stream().map(attrGroupEntity -> {

            ItemGroupVo itemGroupVo = new ItemGroupVo();
            itemGroupVo.setId(attrGroupEntity.getId());
            itemGroupVo.setGroupName(attrGroupEntity.getName());

            // 2.遍历分组查询每个组下的attr
            List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", attrGroupEntity.getId()));
            if (!CollectionUtils.isEmpty(attrEntities)) {

                List<Long> attrIds = attrGroupEntities.stream().map(AttrGroupEntity::getId).collect(Collectors.toList());

                // 收集销售属性和基本属性
                List<AttrValueVo> attrValueVos = new ArrayList<>();

                // 3.attrId结合spuId查询规格参数对应值(基本参数)
                List<SpuAttrValueEntity> spuAttrValueEntities = this.spuAttrValueMapper.selectList(new QueryWrapper<SpuAttrValueEntity>().in("attr_id", attrIds).eq("spu_id", spuId));

                // 4.attrId结合skuId查询规格参数对应值(销售参数)
                List<SkuAttrValueEntity> skuAttrValueEntities = this.skuAttrValueMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().in("attr_id", attrIds).eq("sku_id", skuId));

                if (!CollectionUtils.isEmpty(spuAttrValueEntities)) {
                    attrValueVos.addAll(
                            spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                                AttrValueVo attrValueVo = new AttrValueVo();
                                BeanUtils.copyProperties(spuAttrValueEntity, attrValueVo);
                                return attrValueVo;
                            }).collect(Collectors.toList())
                    );
                }

                if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                    attrValueVos.addAll(
                            skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                                AttrValueVo attrValueVo = new AttrValueVo();
                                BeanUtils.copyProperties(skuAttrValueEntity, attrValueVo);
                                return attrValueVo;
                            }).collect(Collectors.toList())
                    );
                }

                itemGroupVo.setAttrValues(attrValueVos);
            }

            return itemGroupVo;
        }).collect(Collectors.toList());


    }

}