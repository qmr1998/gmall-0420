package com.atguigu.gmall.order.client;

import com.atguigu.gmall.cart.api.GmallCartApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Lee
 * @date 2020-10-19  18:54
 */
@FeignClient("cart-service")
public interface GmallCartClient extends GmallCartApi {

}
