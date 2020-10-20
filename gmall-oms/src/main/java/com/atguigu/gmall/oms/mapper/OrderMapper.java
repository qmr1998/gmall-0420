package com.atguigu.gmall.oms.mapper;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单
 * 
 * @author qmr
 * @email qmr@atguigu.com
 * @date 2020-09-21 23:45:40
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {

    public int updateStatus(@Param("orderToken") String orderToken, @Param("expect") Integer expect, @Param("target") Integer target);


}
