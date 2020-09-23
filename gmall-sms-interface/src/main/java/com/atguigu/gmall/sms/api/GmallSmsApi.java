package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Lee
 * @date 2020-09-23  19:14
 */
public interface GmallSmsApi {

    @PostMapping("sms/skubounds/saveSales")
    public ResponseVo saveSales(@RequestBody SkuSaleVo skuSaleVo);

}
