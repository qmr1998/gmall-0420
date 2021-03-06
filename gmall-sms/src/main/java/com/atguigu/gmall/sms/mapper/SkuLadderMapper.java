package com.atguigu.gmall.sms.mapper;

import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * 商品阶梯价格
 * 
 * @author qmr
 * @email qmr@atguigu.com
 * @date 2020-09-21 19:18:27
 */
@Mapper
@Component
public interface SkuLadderMapper extends BaseMapper<SkuLadderEntity> {
	
}
