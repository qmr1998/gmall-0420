package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.client.GmallPmsClient;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Lee
 * @date 2020-10-09  17:52
 */
@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public static final String KEY_PREFIX = "index:category:";

    @Override
    public List<CategoryEntity> queryLevelOneCategories() {
        // 1、查询缓存中是否存在，若存在，直接从缓存中命中
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + "0");
        if(StringUtils.isNotBlank(json)){
            // 如果缓存中有，直接返回
            return JSON.parseArray(json, CategoryEntity.class);
        }
        ResponseVo<List<CategoryEntity>> responseVo = this.pmsClient.queryAllCategory(0L);
        List<CategoryEntity> categoryEntities = responseVo.getData();
        // 2、若缓存中不存在，远程调用从MySQL中查询出所需数据并存入redis中
        this.redisTemplate.opsForValue().set(KEY_PREFIX + "0", JSON.toJSONString(categoryEntities), 90, TimeUnit.DAYS);
        return categoryEntities;
    }

    @Override
    public List<CategoryEntity> queryLevelTwoWithSubByPid(Long parentId) {

        // 1、查询缓存中是否存在，若存在，直接从缓存中命中
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + parentId);
        if(StringUtils.isNotBlank(json)){
            // 如果缓存中有，直接返回
            return JSON.parseArray(json, CategoryEntity.class);
        }

        // 2、若缓存中不存在，远程调用从MySQL中查询出所需数据并存入redis中
        ResponseVo<List<CategoryEntity>> responseVo = this.pmsClient.queryCategoriesLevelTwoWithSubByParentId(parentId);
        List<CategoryEntity> categoryEntities = responseVo.getData();
        this.redisTemplate.opsForValue().set(KEY_PREFIX + parentId, JSON.toJSONString(categoryEntities), 90, TimeUnit.DAYS);
        return categoryEntities;
    }

}

