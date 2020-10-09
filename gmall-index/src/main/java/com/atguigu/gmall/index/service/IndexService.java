package com.atguigu.gmall.index.service;

import com.atguigu.gmall.pms.entity.CategoryEntity;

import java.util.List;

/**
 * @author Lee
 * @date 2020-10-09  17:51
 */
public interface IndexService {
    List<CategoryEntity> queryLevelOneCategories();

    List<CategoryEntity> queryLevelTwoWithSubByPid(Long parentId);

}
