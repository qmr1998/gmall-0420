package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.pojo.Cart;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.Future;

/**
 * @author Lee
 * @date 2020-10-15  17:05
 */
public interface CartService {

    void addCart(Cart cart);

    Cart queryCartBySkuId(Long skuId);

    ListenableFuture<String> executor1();

    String  executor2();

    List<Cart> queryCarts();

    void updateNum(Cart cart);

    void deleteCartBySkuId(Long skuId);

}
