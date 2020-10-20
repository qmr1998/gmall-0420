package com.atguigu.gmall.order.client;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Lee
 * @date 2020-10-15  17:06
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}
