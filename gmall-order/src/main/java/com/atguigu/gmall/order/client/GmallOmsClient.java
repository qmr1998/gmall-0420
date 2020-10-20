package com.atguigu.gmall.order.client;

import com.atguigu.gmall.oms.api.GmallOmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Lee
 * @date 2020-10-19  18:52
 */
@FeignClient("oms-service")
public interface GmallOmsClient extends GmallOmsApi {
}
