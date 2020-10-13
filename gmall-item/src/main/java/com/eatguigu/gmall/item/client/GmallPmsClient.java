package com.eatguigu.gmall.item.client;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Lee
 * @date 2020-10-12  18:22
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
