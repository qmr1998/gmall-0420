package com.atguigu.gmall.scheduled.job;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.scheduled.mapper.CartMapper;
import com.atguigu.gmall.scheduled.pojo.Cart;
import com.atguigu.gmall.scheduled.mapper.CartMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author Lee
 * @date 2020-10-18  21:43
 */
@Component
public class CartJobHandler {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY = "cart:async:exception";

    private static final String KEY_PREFIX = "cart:info:";

    @Autowired
    private CartMapper cartMapper;

    @XxlJob(value = "AsyncExceptionJobHandler")
    public ReturnT<String> asyncException(String param) {

        // 读取异步执行失败的用户信息
        BoundListOperations<String, String> listOps = this.redisTemplate.boundListOps(KEY);
        String userId = listOps.rightPop();

        while (!StringUtils.isEmpty(userId)) {
            BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
            List<Object> cartJsons = hashOps.values(); // 获取该用户所有的购物车
            // 不管redis中有没有购物车信息，先将 mysql 中的购物车数据删除，再添加即可
            this.cartMapper.delete(new QueryWrapper<Cart>().eq("user_id", userId));

            // 判断redis中的购物车是否为空，不为空需要新增mysql记录
            if (!CollectionUtils.isEmpty(cartJsons)) {
                cartJsons.forEach(cartJson->{
                    Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                    // 新增mysql记录
                    this.cartMapper.insert(cart);
                });
            }

            userId = listOps.rightPop();
        }

        return ReturnT.SUCCESS;
    }

}
