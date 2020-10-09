package com.atguigu.gmall.index.client;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Lee
 * @date 2020-10-09  17:57
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
