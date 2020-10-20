package com.atguigu.gmall.order.client;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Lee
 * @date 2020-10-15  17:05
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
