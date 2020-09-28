package com.atguigu.gmall.search.client;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Lee
 * @date 2020-09-27  18:50
 */
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}
