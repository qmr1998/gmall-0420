package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.SkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * sku信息
 * 
 * @author qmr
 * @email qmr@atguigu.com
 * @date 2020-09-21 18:48:44
 */
@Mapper
@Component
public interface SkuMapper extends BaseMapper<SkuEntity> {
	
}
