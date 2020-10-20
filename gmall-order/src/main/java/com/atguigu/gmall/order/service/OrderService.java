package com.atguigu.gmall.order.service;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.order.vo.OrderConfirmVo;

/**
 * @author Lee
 * @date 2020-10-19  16:50
 */
public interface OrderService {
    OrderConfirmVo confirm();

    OrderEntity submit(OrderSubmitVo orderSubmitVo);

}
