package com.eatguigu.gmall.item.client;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Lee
 * @date 2020-10-12  18:25
 */
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}
