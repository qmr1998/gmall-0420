package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 商品三级分类
 * 
 * @author qmr
 * @email qmr@atguigu.com
 * @date 2020-09-21 18:48:44
 */
@Mapper
@Component
public interface CategoryMapper extends BaseMapper<CategoryEntity> {

    List<CategoryEntity> queryCategoriesByPid(Long parentId);
}
