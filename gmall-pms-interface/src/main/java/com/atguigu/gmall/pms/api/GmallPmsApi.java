package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Lee
 * @date 2020-09-27  18:03
 */
public interface  GmallPmsApi {

    // 搜索功能分页查询spu信息
    @PostMapping("pms/spu/json")
    public ResponseVo<List<SpuEntity>> querySpuJsonByPage(@RequestBody PageParamVo paramVo);

    // 根据spuId查询spu的所有sku信息
    @GetMapping("pms/sku/spu/{spuId}")
    public ResponseVo<List<SkuEntity>> querySkusBySpuId(@PathVariable("spuId") Long spuId);

    // 根据brandId查询品牌信息
    @GetMapping("pms/brand/{id}")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    // 根据categoryId查询分类信息
    @GetMapping("pms/category/{id}")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    @RequestMapping("pms/skuattrvalue/search/{cid}/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySearchSkuAttrValuesByCidAndSkuId(@PathVariable("cid") Long cid, @PathVariable("skuId") Long skuId);

    @RequestMapping("pms/spuattrvalue/search/{cid}/{spuId}")
    public ResponseVo<List<SpuAttrValueEntity>> querySearchSpuAttrValuesByCidAndSpuId(@PathVariable("cid") Long cid, @PathVariable("spuId") Long spuId);

    @GetMapping("pms/spu/{id}")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);

    // 根据父id查询商品分类
    @GetMapping("pms/category/parent/{parentId}")
    public ResponseVo<List<CategoryEntity>> queryAllCategory(@PathVariable("parentId") Long parentId);

    @GetMapping("pms/category/subs/{parentId}")
    public ResponseVo<List<CategoryEntity>> queryCategoriesLevelTwoWithSubByParentId(@PathVariable("parentId") Long parentId);
}
