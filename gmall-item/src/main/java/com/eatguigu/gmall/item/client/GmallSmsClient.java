package com.eatguigu.gmall.item.client;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Lee
 * @date 2020-10-12  18:24
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}
