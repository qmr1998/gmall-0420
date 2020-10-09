package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author Lee
 * @date 2020-10-09  17:51
 */
@Controller
public class IndexController {

    @Autowired
    private IndexService indexService;

    @GetMapping({"/","index"})
    @ApiOperation("查询商品一级分类")
    public String toIndex(Model model){
        // 查询一级分类
        List<CategoryEntity> categoryEntityList = this.indexService.queryLevelOneCategories();
        model.addAttribute("categories", categoryEntityList);
        // TODO: 查询各种广告
        return "index";
    }

    @GetMapping("index/cates/{parentId}")
    @ApiOperation("初始化商品二级分类和三级分类")
    @ResponseBody
    public ResponseVo<List<CategoryEntity>> queryLevelTwoWithSubByPid(@PathVariable("parentId") Long parentId){
        List<CategoryEntity> categoryEntities = this.indexService.queryLevelTwoWithSubByPid(parentId);
        return ResponseVo.ok(categoryEntities);
    }

}
