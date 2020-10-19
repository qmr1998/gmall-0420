package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.SuccessCallback;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Lee
 * @date 2020-10-15  17:04
 */
@Controller
public class CartController {

    @Autowired
    private CartService cartService;


    /**
     * 添加购物车成功，重定向到购物车成功页
     *
     * @param cart
     * @return
     */
    @GetMapping
    public String addCart(Cart cart) {
        if (cart == null || cart.getSkuId() == null) {
            throw new RuntimeException("没有选择添加到购物车的商品信息！");
        }
        this.cartService.addCart(cart);
        return "redirect:http://cart.gmall.com/addCart.html?skuId=" + cart.getSkuId();
    }

    /**
     * 跳转到添加成功页
     *
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("addCart.html")
    public String queryCartBySkuId(@RequestParam("skuId") Long skuId, Model model) {
        Cart cart = this.cartService.queryCartBySkuId(skuId);
        model.addAttribute("cart", cart);
        return "addCart";
    }

    @GetMapping("cart.html")
    public String queryCarts(Model model) {
        List<Cart> carts = this.cartService.queryCarts();
        model.addAttribute("carts", carts);
        return "cart";
    }

    @PostMapping("updateNum")
    public ResponseVo updateNum(@RequestBody Cart cart) {
        this.cartService.updateNum(cart);
        return ResponseVo.ok();
    }

    @PostMapping("deleteCart")
    public ResponseVo deleteCartBySkuId(@RequestParam("skuId") Long skuId) {
        this.cartService.deleteCartBySkuId(skuId);
        return ResponseVo.ok();
    }

    @GetMapping("test")
    @ResponseBody
    public String test() throws ExecutionException, InterruptedException {
//        UserInfo userInfo = LoginInterceptor.getUserInfo();
//        System.out.println("userInfo = " + userInfo);
        /*long now = System.currentTimeMillis();
        System.out.println("controller.test方法开始执行！");
        Future<String> future1 = this.cartService.executor1();
        Future<String> future2 = this.cartService.executor2();
        System.out.println("future1的执行结果：" + future1.get());
        System.out.println("future2的执行结果：" + future2.get());
        System.out.println("controller.test方法结束执行！！！" + (System.currentTimeMillis() - now));*/

        /*long now = System.currentTimeMillis();
        System.out.println("controller.test方法开始执行！");
        this.cartService.executor1().addCallback(new SuccessCallback<String>() {
            @Override
            public void onSuccess(String result) {
                System.out.println("future1的正常执行结果：" + result);
            }
        }, new FailureCallback() {
            @Override
            public void onFailure(Throwable ex) {
                System.out.println("future1执行出错：" + ex.getMessage());
            }
        });
        this.cartService.executor2().addCallback(new SuccessCallback<String>() {
            @Override
            public void onSuccess(String result) {
                System.out.println("future2的正常执行结果：" + result);
            }
        }, new FailureCallback() {
            @Override
            public void onFailure(Throwable ex) {
                System.out.println("future2执行出错：" + ex.getMessage());
            }
        });*/

        long now = System.currentTimeMillis();
        System.out.println("controller.test方法开始执行！");
        this.cartService.executor2();

        System.out.println("controller.test方法结束执行！！！" + (System.currentTimeMillis() - now));


        return "hello cart!";
    }
}
