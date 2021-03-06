package com.atguigu.gmall.pms.controller;

import java.util.List;

import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * sku销售属性&值
 *
 * @author qmr
 * @email qmr@atguigu.com
 * @date 2020-09-21 18:48:44
 */
@Api(tags = "sku销售属性&值 管理")
@RestController
@RequestMapping("pms/skuattrvalue")
public class SkuAttrValueController {

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @RequestMapping("search/{cid}/{skuId}")
    @ApiOperation("搜索功能之根据categoryId和skuId查询SkuAttrValueEntity，主要需要attrId,attrName,attrValue")
    public ResponseVo<List<SkuAttrValueEntity>> querySearchSkuAttrValuesByCidAndSkuId(@PathVariable("cid") Long cid, @PathVariable("skuId") Long skuId) {
        List<SkuAttrValueEntity> skuAttrValueEntities = this.skuAttrValueService.querySearchSkuAttrValuesByCidAndSkuId(cid, skuId);
        return ResponseVo.ok(skuAttrValueEntities);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> querySkuAttrValueByPage(PageParamVo paramVo) {
        PageResultVo pageResultVo = skuAttrValueService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SkuAttrValueEntity> querySkuAttrValueById(@PathVariable("id") Long id) {
        SkuAttrValueEntity skuAttrValue = skuAttrValueService.getById(id);

        return ResponseVo.ok(skuAttrValue);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody SkuAttrValueEntity skuAttrValue) {
        skuAttrValueService.save(skuAttrValue);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody SkuAttrValueEntity skuAttrValue) {
        skuAttrValueService.updateById(skuAttrValue);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids) {
        skuAttrValueService.removeByIds(ids);

        return ResponseVo.ok();
    }

    @GetMapping("spu/{spuId}")
    @ApiOperation("根据spuId查询spu下的所有销售属性")
    public ResponseVo<List<SaleAttrValueVo>> querySkuAttrValuesBySpuId(@PathVariable("spuId") Long spuId) {
        List<SaleAttrValueVo> saleAttrValueVos = this.skuAttrValueService.querySkuAttrValuesBySpuId(spuId);
        return ResponseVo.ok(saleAttrValueVos);
    }

    @GetMapping("sku/{skuId}")
    @ApiOperation("根据skuId查询sku的销售属性")
    public ResponseVo<List<SkuAttrValueEntity>> querySkuAttrValueBySkuId(@PathVariable("skuId") Long skuId) {
        List<SkuAttrValueEntity> skuAttrValueEntities = this.skuAttrValueService.querySkuAttrValueBySkuId(skuId);
        return ResponseVo.ok(skuAttrValueEntities);
    }

    @GetMapping("spu/mapping/{spuId}")
    @ApiOperation("查询spu下skuId及销售属性的映射关系")
    public ResponseVo<String> querySkuIdMappingSaleAttrValueBySpuId(@PathVariable("spuId") Long spuId) {
        String json = this.skuAttrValueService.querySkuIdMappingSaleAttrValueBySpuId(spuId);
        return ResponseVo.ok(json);
    }

}
