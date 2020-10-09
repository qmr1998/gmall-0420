package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.client.GmallSmsClient;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.service.SpuService;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Autowired
    private SpuDescMapper spuDescMapper;

    @Autowired
    private SpuAttrValueService spuAttrValueService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @Autowired
    private GmallSmsClient gmallSmsClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    // 根据categoryId分页查询Spu
    @Override
    public PageResultVo querySpuByCidPage(Long cid, PageParamVo pageParamVo) {

        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();

        // 若cid为0，则全站搜索，不为0则本类搜索
        if (cid != 0) {
            wrapper.eq("category_id", cid);
        }

        // 从pageParamVo取出搜索条件key，如果用户输入了检索条件，根据检索条件查
        // 示例sql: SELECT * FROM pms_spu WHERE category_id = 225 AND (id = 7 or name LIKE '%7%');
        String key = pageParamVo.getKey();
        if (!StringUtils.isBlank(key)) {
            wrapper.and(t -> t.eq("id", key).or().like("name", key));
        }

        IPage<SpuEntity> page = this.page(
                pageParamVo.getPage(),
                wrapper
        );

        return new PageResultVo(page);
    }

    // 保存spu信息（包含9张表）
    @Override
    @GlobalTransactional
    public void bigSave(SpuVo spu) {
        // 1.保存spu相关信息
        // 1.1 保存 pms_spu
        spu.setPublishStatus(1); // 默认是已上架
        spu.setCreateTime(new Date()); // 设置新增时间
        spu.setUpdateTime(spu.getCreateTime()); // 设置修改时间
        baseMapper.insert(spu); // 插入pms_spu
        Long spuId = spu.getId(); // // 获取新增后的spuId ，后边会用到

        // 1.2 保存 pms_spu_desc
        if (!CollectionUtils.isEmpty(spu.getSpuImages())) { // 若图片的url不为空，则插入数据库
            SpuDescEntity spuDescEntity = new SpuDescEntity(); // 创建SpuDescEntity对象
            // 注意：spu_info_desc表的主键是spu_id,需要在实体类中配置该主键不是自增主键
            spuDescEntity.setSpuId(spuId); // 设置spuId
            // 把商品的图片描述，保存到spu详情中，图片地址以逗号进行分割
            spuDescEntity.setDecript(StringUtils.join(spu.getSpuImages(), ","));
            this.spuDescMapper.insert(spuDescEntity); // 插入pms_spu_desc
        }

        // 1.3 保存 pms_spu_attr_value
        List<SpuAttrValueVo> baseAttrs = spu.getBaseAttrs(); // baseAttrs对应 pms_spu_attr_value中的数据
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            this.spuAttrValueService.saveBatch(
                    baseAttrs.stream().map(spuAttrValueVo -> {
                        SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
                        BeanUtils.copyProperties(spuAttrValueVo, spuAttrValueEntity);
                        spuAttrValueEntity.setSpuId(spuId);
                        spuAttrValueEntity.setSort(0);
                        return spuAttrValueEntity;
                    }).collect(Collectors.toList()));
        }

        // 2.保存sku相关信息
        List<SkuVo> skus = spu.getSkus();
        if (!CollectionUtils.isEmpty(skus)) {
            skus.forEach(skuVo -> {
                // 2.1 保存 pms_sku
                skuVo.setSpuId(spuId);
                skuVo.setCatagoryId(spu.getCategoryId()); // pms_sku表中有但是我们前台没有传递的字段，需要在这里手动设置
                skuVo.setBrandId(spu.getBrandId()); // pms_sku表中有但是我们前台没有传递的字段，需要在这里手动设置
                List<String> images = skuVo.getImages(); // pms_sku表中有但是我们前台没有传递的字段，需要在这里手动设置
                if (!CollectionUtils.isEmpty(images)) {
                    // 如果用户传进来了默认图片就使用用户传递的，若没传递，就取出图片的第一张作为默认图片
                    skuVo.setDefaultImage(StringUtils.isNotBlank(skuVo.getDefaultImage()) ? skuVo.getDefaultImage() : images.get(0));
                }
                this.skuMapper.insert(skuVo);
                Long skuId = skuVo.getId();

                // 2.2 保存 pms_sku_images
                if (!CollectionUtils.isEmpty(images)) {
                    skuImagesService.saveBatch(
                            // 将 images 集合转化为 skuImageEntity 集合，就可以批量插入数据库，skuImageEntity对应 pms_sku_images 表，属性有 id,skuId,url,sort,defaultStatus
                            images.stream().map(image -> {
                                SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                                skuImagesEntity.setSkuId(skuId);
                                skuImagesEntity.setUrl(image);
                                skuImagesEntity.setSort(0);
                                // 如果是默认图就将defaultStatus设置为1
                                if (StringUtils.equals(image, skuVo.getDefaultImage())) {
                                    skuImagesEntity.setDefaultStatus(1);
                                }
                                return skuImagesEntity;
                            }).collect(Collectors.toList()));
                }

                // 2.3 保存 pms_sku_attr_value
                List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
                // 前台传递的saleAttrs里包含attrId、attrName、attrValue，但是 SkuAttrValueEntity 还有 sku_id(必须) 和 sort(非必须) 字段
                if (!CollectionUtils.isEmpty(saleAttrs)) {
                    saleAttrs.forEach(skuAttrValueEntity -> {
                        skuAttrValueEntity.setSkuId(skuId);
                        skuAttrValueEntity.setSort(0);
                    });
                    this.skuAttrValueService.saveBatch(saleAttrs);
                }

                // 3.保存sku营销相关信息
                // 3.1 保存 sms_sku_bounds
                // 3.2 保存 sms_full_reduction
                // 3.3 保存 sms_sku_ladder
                // 调用远程接口来实现
                SkuSaleVo skuSaleVo = new SkuSaleVo();
                BeanUtils.copyProperties(skuVo, skuSaleVo);
                skuSaleVo.setSkuId(skuId);
                this.gmallSmsClient.saveSales(skuSaleVo);


            });
        }


        this.rabbitTemplate.convertAndSend("PMS_ITEM_EXCHANGE","item.insert",spuId);

    }

}