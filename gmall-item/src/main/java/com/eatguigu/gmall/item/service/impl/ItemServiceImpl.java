package com.eatguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.extension.api.R;
import com.eatguigu.gmall.item.client.GmallPmsClient;
import com.eatguigu.gmall.item.client.GmallSmsClient;
import com.eatguigu.gmall.item.client.GmallWmsClient;
import com.eatguigu.gmall.item.service.ItemService;
import com.eatguigu.gmall.item.vo.ItemVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author Lee
 * @date 2020-10-12  20:37
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public ItemVo load(Long skuId) {

        ItemVo itemVo = new ItemVo();

        // 根据skuId查询sku的信息 1
        CompletableFuture<SkuEntity> skuEntityCompletableFuture = CompletableFuture.supplyAsync(() -> {
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(skuId);
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                return null;
            }
            itemVo.setSkuId(skuId);
            itemVo.setTitle(skuEntity.getTitle());
            itemVo.setSubTitle(skuEntity.getSubtitle());
            itemVo.setPrice(skuEntity.getPrice());
            itemVo.setWeight(skuEntity.getWeight());
            itemVo.setDefaultImage(skuEntity.getDefaultImage());
            return skuEntity;
        }, threadPoolExecutor);

        // 根据cid3查询分类信息 2
        CompletableFuture<Void> cateCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<CategoryEntity>> categoryResponseVo = this.pmsClient.queryCategoriesByCid3(skuEntity.getCatagoryId());
            List<CategoryEntity> categoryEntities = categoryResponseVo.getData();
            itemVo.setCategories(categoryEntities);
        }, threadPoolExecutor);

        // 根据品牌的id查询品牌 3
        CompletableFuture<Void> brandCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<BrandEntity> brandEntityResponseVo = this.pmsClient.queryBrandById(skuEntity.getBrandId());
            BrandEntity brandEntity = brandEntityResponseVo.getData();
            if (brandEntity != null) {
                itemVo.setBrandId(brandEntity.getId());
                itemVo.setBrandName(brandEntity.getName());
            }
        }, threadPoolExecutor);

        // 根据spuId查询spu 4
        CompletableFuture<Void> spuCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<SpuEntity> spuEntityResponseVo = this.pmsClient.querySpuById(skuEntity.getSpuId());
            SpuEntity spuEntity = spuEntityResponseVo.getData();
            if (spuEntity != null) {
                itemVo.setSpuId(spuEntity.getId());
                itemVo.setSpuName(spuEntity.getName());
            }
        }, threadPoolExecutor);


        // 跟据skuId查询图片 5
        CompletableFuture<Void> skuImagesCompletableFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuImagesEntity>> skuImagesResponseVo = this.pmsClient.queryImagesBySkuId(skuId);
            List<SkuImagesEntity> skuImagesEntities = skuImagesResponseVo.getData();
            itemVo.setImages(skuImagesEntities);
        });

        // 根据skuId查询sku营销信息 6
        CompletableFuture<Void> salesCompletableFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<ItemSaleVo>> salesResponseVo = this.smsClient.querySalesBySkuId(skuId);
            List<ItemSaleVo> itemSaleVos = salesResponseVo.getData();
            itemVo.setSales(itemSaleVos);
        });

        // 根据skuId查询sku的库存信息 7
        CompletableFuture<Void> storeCompletableFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<WareSkuEntity>> wareResponseVo = this.wmsClient.queryWareSkusBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }
        });


        // 根据spuId查询spu下的所有sku的销售属性 8
        CompletableFuture<Void> saleAttrsCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<SaleAttrValueVo>> saleAttrResponseVo = this.pmsClient.querySkuAttrValuesBySpuId(skuEntity.getSpuId());
            List<SaleAttrValueVo> saleAttrValueVos = saleAttrResponseVo.getData();
            itemVo.setSaleAttrs(saleAttrValueVos);
        });

        // 当前sku的销售属性 9
        CompletableFuture<Void> saleAttrCompletableFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuAttrValueEntity>> skuAttrValueResponseVo = this.pmsClient.querySkuAttrValueBySkuId(skuId);
            List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueResponseVo.getData();
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                Map<Long, String> map = skuAttrValueEntities.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue));
                itemVo.setSaleAttr(map);
            }
        });

        // 根据spuId查询spu下的所有sku及销售属性的映射关系 10
        CompletableFuture<Void> mappingCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<String> stringResponseVo = this.pmsClient.querySkuIdMappingSaleAttrValueBySpuId(skuEntity.getSpuId());
            String json = stringResponseVo.getData();
            itemVo.setSkuJsons(json);
        });

        // 根据spuId查询spu的海报信息(商品描述信息) 11
        CompletableFuture<Void> spuImagesCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<SpuDescEntity> spuDescEntityResponseVo = this.pmsClient.querySpuDescById(skuEntity.getSpuId());
            SpuDescEntity spuDescEntity = spuDescEntityResponseVo.getData();
            if (spuDescEntity != null) {
                String[] urls = StringUtils.split(spuDescEntity.getDecript(), ",");
                itemVo.setSpuImages(Arrays.asList(urls));
            }
        });

        // 根据cid3 spuId skuId查询组及组下的规格参数及值 12
        CompletableFuture<Void> groupCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<ItemGroupVo>> itemGroupResponseVo = this.pmsClient.queryGroupsBySpuIdAndSkuIdAndCid(skuEntity.getSpuId(), skuId, skuEntity.getCatagoryId());
            List<ItemGroupVo> itemGroupVos = itemGroupResponseVo.getData();
            itemVo.setGroups(itemGroupVos);
        });


        // 等待所有任务完成
        CompletableFuture.allOf(
                cateCompletableFuture,
                brandCompletableFuture,
                spuCompletableFuture,
                skuImagesCompletableFuture,
                salesCompletableFuture,
                storeCompletableFuture,
                saleAttrsCompletableFuture,
                saleAttrCompletableFuture,
                mappingCompletableFuture,
                spuImagesCompletableFuture,
                groupCompletableFuture
        ).join();

        return itemVo;
    }
}
