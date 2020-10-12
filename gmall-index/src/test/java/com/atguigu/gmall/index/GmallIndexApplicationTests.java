package com.atguigu.gmall.index;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.client.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.util.List;

@SpringBootTest
class GmallIndexApplicationTests {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RBloomFilter<String> bloomFilter;

    @Test
    void contextLoads() {
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryAllCategory(0L);
        List<CategoryEntity> categoryEntities = listResponseVo.getData();
        if (!CollectionUtils.isEmpty(categoryEntities)) {
            categoryEntities.forEach(categoryEntity -> {
                bloomFilter.add(categoryEntity.getId().toString());
            });
        }
    }

}
