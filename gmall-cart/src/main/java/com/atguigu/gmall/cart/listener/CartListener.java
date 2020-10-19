package com.atguigu.gmall.cart.listener;

import com.atguigu.gmall.cart.client.GmallPmsClient;
import com.atguigu.gmall.cart.client.GmallSmsClient;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.cart.service.impl.CartServiceImpl;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SkuEntity;
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
 * @date 2020-10-17  19:56
 */
@Component
public class CartListener {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String PRICE_PREFIX = "cart:price:";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("CART_PMS_QUEUE"),
            exchange = @Exchange(value = "PMS_SPU_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"item.update"}
    ))
    public void listener(Long spuId, Channel channel, Message message) throws IOException {
        if (spuId == null) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        ResponseVo<List<SkuEntity>> listResponseVo = this.pmsClient.querySkusBySpuId(spuId);
        List<SkuEntity> skuEntities = listResponseVo.getData();
        if (!CollectionUtils.isEmpty(skuEntities)) {
            skuEntities.forEach(skuEntity -> {
                String price = this.redisTemplate.opsForValue().get(PRICE_PREFIX + skuEntity.getId());
                if (StringUtils.isNotBlank(price)) { // 不为空表示缓存中有，重新set一下，设置新的价格
                    this.redisTemplate.opsForValue().set(PRICE_PREFIX + skuEntity.getId(), skuEntity.getPrice().toString());
                }
            });
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}
