package com.atguigu.gmall.order.client;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Lee
 * @date 2020-10-19  18:54
 */
@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {
}
