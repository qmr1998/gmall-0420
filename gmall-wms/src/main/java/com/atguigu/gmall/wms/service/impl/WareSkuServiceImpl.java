package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import org.springframework.util.CollectionUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuMapper, WareSkuEntity> implements WareSkuService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private WareSkuMapper wareSkuMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String KEY_PREFIX = "stock:lock:";

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<WareSkuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuLockVo> checkAndLock(List<SkuLockVo> lockVos, String orderToken) {

        // 判断lockVos是否为空
        if (CollectionUtils.isEmpty(lockVos)) {
            return null;
        }

        // 遍历所有商品，验库存并锁定库存
        // 每一个商品验库存并锁库存
        lockVos.forEach(this::checkLock);

        // 如果有一个商品锁定失败了，所有已经成功锁定的商品要解库存
        List<SkuLockVo> successLockVos = lockVos.stream().filter(SkuLockVo::getLock).collect(Collectors.toList());
        List<SkuLockVo> errorLockVos = lockVos.stream().filter(skuLockVo -> !skuLockVo.getLock()).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(errorLockVos)) {
            successLockVos.forEach(lockVo -> this.wareSkuMapper.unlockStock(lockVo.getWareSkuId(), lockVo.getCount()));
            return lockVos;
        }

        // 把库存的锁定信息保存到redis中，以方便将来解锁库存
        this.redisTemplate.opsForValue().set(KEY_PREFIX + orderToken, JSON.toJSONString(lockVos));

        // 为了防止宕机导致的锁死情况，要定时解锁库存
        this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "stock.ttl", orderToken);

        // 如果都锁定成功，不需要展示锁定情况
        return null;
    }

    private void checkLock(SkuLockVo skuLockVo) {
        RLock fairLock = this.redissonClient.getFairLock("lock:" + skuLockVo.getSkuId());
        fairLock.lock();

        try {
            // 验库存
            List<WareSkuEntity> wareSkuEntities = this.wareSkuMapper.checkStock(skuLockVo.getSkuId(), skuLockVo.getCount());
            if (CollectionUtils.isEmpty(wareSkuEntities)) {
                skuLockVo.setLock(false); // 库存不足，锁定失败
                return;
            }
            // 库存充足，锁库存。一般会根据运输距离，就近调配。这里就锁定第一个仓库的库存
            if (this.wareSkuMapper.lockStock(wareSkuEntities.get(0).getId(), skuLockVo.getCount()) == 1) {
                skuLockVo.setLock(true);  // 锁定成功
                skuLockVo.setWareSkuId(wareSkuEntities.get(0).getId());
            } else {
                skuLockVo.setLock(false);
            }

        } finally {
            fairLock.unlock(); // 程序返回之前，一定要释放锁
        }

    }

}