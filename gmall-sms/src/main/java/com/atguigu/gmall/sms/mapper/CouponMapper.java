package com.atguigu.gmall.sms.mapper;

import com.atguigu.gmall.sms.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author qmr
 * @email qmr@atguigu.com
 * @date 2020-09-21 19:18:27
 */
@Mapper
public interface CouponMapper extends BaseMapper<CouponEntity> {
	
}
