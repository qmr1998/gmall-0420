package com.atguigu.gmall.pms.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Lee
 * @date 2020-10-12  19:49
 */
@SpringBootTest
class SkuAttrValueMapperTest {

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Test
    void querySkuIdMappingSaleAttrValueBySpuId(){
        System.out.println(this.skuAttrValueMapper.querySkuIdMappingSaleAttrValueBySpuId(7L));
    }

}