package com.atguigu.gmall.auth.client;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Lee
 * @date 2020-10-14  18:08
 */
@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {
}
