package com.atguigu.gmall.pms.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }

    // 根据父id查询商品分类
    @Override
    public List<CategoryEntity> queryCategory(Long parentId) {

        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();

        // 构造查询条件
        // 如果parentId为-1，表示查询所有分类，不需要 wrapper
        if (parentId != -1) {
            wrapper.eq("parent_id", parentId);
        }

        return baseMapper.selectList(wrapper);
    }

    // 根据父id查询商品子分类（二级分类）
    @Override
    public List<CategoryEntity> queryCategoriesLevelTwoWithSubByParentId(Long parentId) {
        List<CategoryEntity> categoryEntityList = this.categoryMapper.queryCategoriesByPid(parentId);
        return categoryEntityList;
    }


}