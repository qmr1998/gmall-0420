package com.atguigu.gmall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.client.GmallPmsClient;
import com.atguigu.gmall.cart.client.GmallSmsClient;
import com.atguigu.gmall.cart.client.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.concurrent.ListenableFuture;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Lee
 * @date 2020-10-15  17:05
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private CartAsyncService cartAsyncService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "cart:info:";

    private static final String PRICE_PREFIX = "cart:price:";

    private String getUserId() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        // 如果用户的id不为空，说明该用户已登录，添加购物车应该以userId作为key
        Long userId = userInfo.getUserId();
        if (userId == null) {
            return userInfo.getUserKey();
        }
        // 否则，说明用户未登录，以userKey作为key
        return userId.toString();
    }

    @Override
    public void addCart(Cart cart) {
        // 1.获取登录信息
        String userId = getUserId();
        // 外层map的key
        String key = KEY_PREFIX + userId;

        // 2.这个hashOps相当于内层的map一样取redis中该用户的购物车，这个hashOps相当于内层的map一样
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        // 3.判断该用户的购物车信息是否已包含了该商品
        String skuIdString = cart.getSkuId().toString();
        BigDecimal count = cart.getCount(); // 用户添加的商品数量
        if (hashOps.hasKey(skuIdString)) {
            // 4.包含，更新数量
            String cartJson = hashOps.get(skuIdString).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(cart.getCount().add(count));

            // 更新到mysql
            this.cartAsyncService.updateCart(userId, cart);
        } else {
            // 5.不包含，给该用户新增购物车记录 skuId count
            cart.setUserId(userId);

            // 根据skuId查询sku
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity != null) {
                cart.setTitle(skuEntity.getTitle());
                cart.setPrice(skuEntity.getPrice());
                cart.setDefaultImage(skuEntity.getDefaultImage());
            }

            // 根据skuId查询销售属性
            ResponseVo<List<SkuAttrValueEntity>> skuAttrValueResponseVo = this.pmsClient.querySkuAttrValueBySkuId(cart.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueResponseVo.getData();
            cart.setSaleAttrs(JSON.toJSONString(skuAttrValueEntities));

            //  根据skuId查询营销信息
            ResponseVo<List<ItemSaleVo>> itemSaleResponseVo = this.smsClient.querySalesBySkuId(cart.getSkuId());
            List<ItemSaleVo> itemSaleVos = itemSaleResponseVo.getData();
            cart.setSales(JSON.toJSONString(itemSaleVos));

            // 根据skuId查询库存信息
            ResponseVo<List<WareSkuEntity>> wareSkuResponseVo = this.wmsClient.queryWareSkusBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = wareSkuResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }

            // 商品刚加入购物车时，默认为选中状态
            cart.setCheck(true);

            // 新增到mysql
            this.cartAsyncService.insertCart(userId, cart);

            // 添加实时价格缓存到redis
            if (skuEntity != null) {
                this.redisTemplate.opsForValue().set(PRICE_PREFIX + skuIdString, skuEntity.getPrice().toString());
            }

        }

        // 新增到redis
        hashOps.put(skuIdString, JSON.toJSONString(cart));
    }

    /**
     * 加入购物车成功之后，成功信息的回显
     *
     * @param skuId
     * @return
     */
    @Override
    public Cart queryCartBySkuId(Long skuId) {
        // 1.获取用户的登录信息
        String userId = getUserId();
        // 外层map的key
        String key = KEY_PREFIX + userId;

        // 2.获取redis中该用户的购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        if (hashOps.hasKey(skuId.toString())) {
            String cartJson = hashOps.get(skuId.toString()).toString();
            return JSON.parseObject(cartJson, Cart.class);
        }

        throw new RuntimeException("您的购物车中没有该商品记录！");
    }

    @Override
    public List<Cart> queryCarts() {
        // 1.获取userKey，查询未登录的购物车
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userKey = userInfo.getUserKey();
        String unloginKey = KEY_PREFIX + userKey;
        // 获取了未登录的购物车
        BoundHashOperations<String, Object, Object> unloginHashOps = this.redisTemplate.boundHashOps(unloginKey);
        // 获取未登录购物车的json集合
        List<Object> cartJsons = unloginHashOps.values();
        List<Cart> unloginCarts = null;
        // 反序列化为cart集合
        if (!CollectionUtils.isEmpty(cartJsons)) {
            unloginCarts = cartJsons.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                // 设置实时价格
                cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId())));
                return cart;
            }).collect(Collectors.toList());
        }

        // 2.获取userId，判断是否登录。未登录则直接返回未登录的购物车
        Long userId = userInfo.getUserId();
        if (userId == null) {
            return unloginCarts;
        }

        // 3.判断有没有未登录的购物车，有则合并
        String loginKey = KEY_PREFIX + userId;
        // 获取了登录状态购物车操作对象
        BoundHashOperations<String, Object, Object> loginHashOps = this.redisTemplate.boundHashOps(loginKey);
        // 判断是否存在未登录的购物车，有则遍历未登录的购物车合并到已登录的购物车中去
        if (!CollectionUtils.isEmpty(unloginCarts)) {
            unloginCarts.forEach(cart -> {
                // 登录状态购物车已存在该商品，更新数量
                if (loginHashOps.hasKey(cart.getSkuId().toString())) {
                    // 未登录购物车当前商品的数量
                    BigDecimal count = cart.getCount();
                    // 获取登录状态的购物车并反序列化
                    String cartJson = loginHashOps.get(cart.getSkuId().toString()).toString();
                    cart = JSON.parseObject(cartJson, Cart.class);
                    cart.setCount(cart.getCount().add(count));

                    // 写回mysql和redis
                    this.cartAsyncService.updateCart(userId.toString(), cart);
                } else {
                    // 新增购物车记录
                    cart.setUserId(userId.toString());
                    // 更新到mysql
                    cartAsyncService.insertCart(userId.toString(), cart);
                }
                // 更新到redis
                loginHashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
            });
        }

        // 4.删除未登录的购物车
        this.redisTemplate.delete(unloginKey);
        this.cartAsyncService.deleteCartByUserId(userKey);

        // 5.查询登录状态所有购物车信息，反序列化后返回
        List<Object> loginCartJsons = loginHashOps.values();
        if (!CollectionUtils.isEmpty(loginCartJsons)) {
            return loginCartJsons.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId())));
                return cart;
            }).collect(Collectors.toList());
        }

        return null;
    }

    @Override
    public void updateNum(Cart cart) {
        String userId = getUserId();
        String key = KEY_PREFIX + userId;

        // 获取该用户的所有购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        // 判断该用户的购物车中是否包含该条信息
        if (hashOps.hasKey(cart.getSkuId().toString())) {
            BigDecimal count = cart.getCount(); // 页面传递的需要更新的数量
            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(count);

            // 写回mysql
            this.cartAsyncService.updateCart(userId, cart);
            // 写回redis
            hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
        }
    }

    @Override
    public void deleteCartBySkuId(Long skuId) {
        String userId = getUserId();

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);

        if (hashOps.hasKey(skuId.toString())) {
            // 删除redis中数据
            hashOps.delete(skuId.toString());

            // 删除mysql中数据
            this.cartAsyncService.deleteCartByUserIdAndSkuId(userId, skuId);
        }
    }

    @Async
    @Override
    public ListenableFuture<String> executor1() {
        try {
            System.out.println("executor1方法开始执行");
            TimeUnit.SECONDS.sleep(4);
            System.out.println("executor1方法结束执行。。。");
            return AsyncResult.forValue("executor1"); // 正常响应
        } catch (InterruptedException e) {
            e.printStackTrace();
            return AsyncResult.forExecutionException(e); // 异常响应
        }
    }

    @Async
    @Override
    public String executor2() {
        try {
            System.out.println("executor2方法开始执行");
            TimeUnit.SECONDS.sleep(5);
            System.out.println("executor2方法结束执行。。。");
            int i = 1 / 0; // 制造异常
            return "executor2"; // 正常响应
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


}
