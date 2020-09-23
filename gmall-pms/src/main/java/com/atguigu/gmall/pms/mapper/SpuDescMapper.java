package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.SpuDescEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * spu信息介绍
 * 
 * @author qmr
 * @email qmr@atguigu.com
 * @date 2020-09-21 18:48:44
 */
@Mapper
@Component
public interface SpuDescMapper extends BaseMapper<SpuDescEntity> {
	
}
