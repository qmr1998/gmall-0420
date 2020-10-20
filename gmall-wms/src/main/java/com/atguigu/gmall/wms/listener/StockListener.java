package com.atguigu.gmall.wms.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;


/**
 * @author Lee
 * @date 2020-10-20  19:53
 */
@Component
public class StockListener {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private WareSkuMapper wareSkuMapper;

    private static final String KEY_PREFIX = "stock:lock:";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "STOCK_UNLOCK_QUEUE", durable = "true"),
            exchange = @Exchange(value = "ORDER_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"stock:unlock"}
    ))
    public void unlock(String orderToken, Channel channel, Message message) throws IOException {
        String lockString = this.redisTemplate.opsForValue().get(KEY_PREFIX + orderToken);
        // 如果锁定库存信息为空，不用做任何处理
        if (StringUtils.isBlank(lockString)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 若不为空，说明有解锁信息，需要反序列化
        List<SkuLockVo> SkuLockVos = JSON.parseArray(lockString, SkuLockVo.class);
        if (CollectionUtils.isEmpty(SkuLockVos)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 解锁
        SkuLockVos.forEach(skuLockVo -> this.wareSkuMapper.unlockStock(skuLockVo.getSkuId(), skuLockVo.getCount()));
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
