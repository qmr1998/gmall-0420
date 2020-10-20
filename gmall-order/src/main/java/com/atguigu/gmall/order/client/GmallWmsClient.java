package com.atguigu.gmall.order.client;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Lee
 * @date 2020-10-15  17:06
 */
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}
