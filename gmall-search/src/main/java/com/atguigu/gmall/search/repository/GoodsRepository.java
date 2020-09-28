package com.atguigu.gmall.search.repository;

import com.atguigu.gmall.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author Lee
 * @date 2020-09-27  18:58
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods, Long> {
}
