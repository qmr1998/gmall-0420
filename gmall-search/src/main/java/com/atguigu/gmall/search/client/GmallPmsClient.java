package com.atguigu.gmall.search.client;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Lee
 * @date 2020-09-27  18:49
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
