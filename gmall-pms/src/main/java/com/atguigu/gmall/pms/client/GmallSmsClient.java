package com.atguigu.gmall.pms.client;


import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;


/**
 * @author Lee
 * @date 2020-09-23  18:45
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {

}
