package com.atguigu.gmall.search.service.impl;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseAttrValueVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Lee
 * @date 2020-09-27  21:08
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public SearchResponseVo search(SearchParamVo paramVo) {
        try {
            SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, this.buildDsl(paramVo));
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            System.out.println("searchResponse = " + searchResponse);

            // paramVo里包含pageNum和pageSize，而searchResponse内不包含，所以在这里设置这两个属性值
            SearchResponseVo responseVo = parseResult(searchResponse);
            responseVo.setPageNum(paramVo.getPageNum());
            responseVo.setPageSize(paramVo.getPageSize());
            return responseVo; // 解析结果集并返回

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 解析搜索后响应的结果集
    private SearchResponseVo parseResult(SearchResponse searchResponse) {
        SearchResponseVo responseVo = new SearchResponseVo();

        // 总命中数
        SearchHits hits = searchResponse.getHits();
        responseVo.setTotal(hits.getTotalHits());

        // 获取当前页的记录
        SearchHit[] hitsHits = hits.getHits();
        List<Goods> goodsList = Stream.of(hitsHits).map(hitsHit -> {
            try {
                String json = hitsHit.getSourceAsString(); // 拿到json字符串然后进行反序列化
                // 反序列化
                Goods goods = MAPPER.readValue(json, Goods.class);
                // 用高亮标题来覆盖普通的标题
                Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
                HighlightField highlightField = highlightFields.get("title"); // 获取高亮的字段
                Text[] fragments = highlightField.getFragments(); // 拿到高亮的具体值
                goods.setTitle(fragments[0].string());
                return goods;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null; // 有异常就返回null
        }).collect(Collectors.toList());
        responseVo.setGoodsList(goodsList);

        // 获取所有聚合结果集
        Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().asMap();

        // 解析品牌聚合结果集获取品牌过滤信息
        ParsedLongTerms brandIdAgg = (ParsedLongTerms) aggregationMap.get("brandIdAgg");
        List<? extends Terms.Bucket> brandBuckets = brandIdAgg.getBuckets(); // 拿到桶
        if (!CollectionUtils.isEmpty(brandBuckets)) {
            List<BrandEntity> brandEntities = brandBuckets.stream().map(bucket -> {
                BrandEntity brandEntity = new BrandEntity();
                // 设置品牌id
                brandEntity.setId(bucket.getKeyAsNumber().longValue());
                // 解析子聚合，获取品牌名称和logo
                Map<String, Aggregation> subAggregationMap = bucket.getAggregations().asMap();
                // 解析品牌名称
                ParsedStringTerms brandNameAgg = (ParsedStringTerms) subAggregationMap.get("brandNameAgg");
                List<? extends Terms.Bucket> nameAggBuckets = brandNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(nameAggBuckets)) {
                    brandEntity.setName(nameAggBuckets.get(0).getKeyAsString());
                }
                // 解析品牌logo
                ParsedStringTerms logoAgg = (ParsedStringTerms) subAggregationMap.get("logoAgg");
                List<? extends Terms.Bucket> logoAggBuckets = logoAgg.getBuckets();
                if (!CollectionUtils.isEmpty(logoAggBuckets)) {
                    brandEntity.setLogo(logoAggBuckets.get(0).getKeyAsString());
                }
                return brandEntity;
            }).collect(Collectors.toList());

            responseVo.setBrands(brandEntities);
        }

        // 解析分类聚合结果集获取分类信息
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms) aggregationMap.get("categoryIdAgg");
        List<? extends Terms.Bucket> categoryBuckets = categoryIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(categoryBuckets)) {
            List<CategoryEntity> categoryEntities = categoryBuckets.stream().map(bucket -> {
                CategoryEntity categoryEntity = new CategoryEntity();
                // 设置分类id
                categoryEntity.setId(bucket.getKeyAsNumber().longValue());
                // 解析子聚合，获取分类名称
                ParsedStringTerms categoryNameAgg = bucket.getAggregations().get("categoryNameAgg");
                List<? extends Terms.Bucket> nameAggBuckets = categoryNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(nameAggBuckets)) {
                    // 解析分类名称
                    categoryEntity.setName(nameAggBuckets.get(0).getKeyAsString());
                }

                return categoryEntity;
            }).collect(Collectors.toList());

            responseVo.setCategories(categoryEntities);
        }

        // 解析规格参数聚合结果集获取规格参数信息
        ParsedNested attrAgg = (ParsedNested) aggregationMap.get("attrAgg");
        // 解析子聚合
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> attrIdAggBuckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(attrIdAggBuckets)) {
            List<SearchResponseAttrValueVo> filters = attrIdAggBuckets.stream().map(bucket -> {
                SearchResponseAttrValueVo attrValueVo = new SearchResponseAttrValueVo();
                // 解析attrId
                attrValueVo.setAttrId(bucket.getKeyAsNumber().longValue());

                // 获取每个规格参数id下的子聚合
                Map<String, Aggregation> subAggregationMap = bucket.getAggregations().asMap();
                // 解析attrName
                ParsedStringTerms attrNameAgg = (ParsedStringTerms) subAggregationMap.get("attrNameAgg");
                List<? extends Terms.Bucket> nameAggBuckets = attrNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(nameAggBuckets)) {
                    attrValueVo.setAttrName(nameAggBuckets.get(0).getKeyAsString());
                }

                // 解析attrValues
                ParsedStringTerms attrValueAgg = (ParsedStringTerms) subAggregationMap.get("attrValueAgg");
                List<? extends Terms.Bucket> valueAggBuckets = attrValueAgg.getBuckets();
                if (!CollectionUtils.isEmpty(valueAggBuckets)) {
                    attrValueVo.setAttrValues(valueAggBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList()));
                }

                return attrValueVo;
            }).collect(Collectors.toList());

            responseVo.setFilters(filters);
        }

        return responseVo;
    }

    // 构建dsl语句的方法，接受页面参数来去搜索
    private SearchSourceBuilder buildDsl(SearchParamVo paramVo) {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        String keyword = paramVo.getKeyword();
        if (StringUtils.isBlank(keyword)) {
            // TODO：打广告
            return sourceBuilder;
        }

        // 1、构建搜索条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        sourceBuilder.query(boolQueryBuilder);

        // 1.1、构建匹配查询
        boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND));

        // 1.2、构建过滤条件
        // 1.2.1、品牌过滤
        List<Long> brandId = paramVo.getBrandId();
        if (!CollectionUtils.isEmpty(brandId)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brandId));
        }

        // 1.2.2、分类过滤
        List<Long> cid3 = paramVo.getCid3();
        if (!CollectionUtils.isEmpty(cid3)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId", cid3));
        }

        // 1.2.3、价格区间过滤
        Double priceFrom = paramVo.getPriceFrom();
        Double priceTo = paramVo.getPriceTo();
        if (priceFrom != null || priceTo != null) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price");
            if (priceFrom != null) {
                rangeQueryBuilder.gte(priceFrom);
            }
            if (priceTo != null) {
                rangeQueryBuilder.lte(priceTo);
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }

        // 1.2.4、库存过滤
        Boolean store = paramVo.getStore();
        if (store != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("store", store));
        }

        // 1.2.5、规格参数的嵌套过滤 eg:["4:8G-12G", "5:128G-256G-521G"]
        List<String> props = paramVo.getProps();
        if (!CollectionUtils.isEmpty(props)) {
            props.forEach(prop -> {
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                // 先对 : 进行切割,若切割后长度为2表示跟我们规定的参数一致
                String[] attr = StringUtils.split(prop, ":");
                if (attr != null && attr.length == 2) {
                    boolQuery.must(QueryBuilders.termQuery("searchAttrs.attrId", attr[0]));

                    // 再对 "-"进行切割，拿到该属性的每一个值
                    String[] attrValues = StringUtils.split(attr[1], "-");
                    boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrValue", attrValues));
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs", boolQuery, ScoreMode.None));
                }
            });
        }

        // 2、构建排序条件 : 1-价格升序 2-价格降序 3-新品降序 4-销量降序
        Integer sort = paramVo.getSort();
        if (sort != null) {
            switch (sort) {
                case 1:
                    sourceBuilder.sort("price", SortOrder.ASC);
                    break;
                case 2:
                    sourceBuilder.sort("price", SortOrder.DESC);
                    break;
                case 3:
                    sourceBuilder.sort("createTime", SortOrder.DESC);
                    break;
                case 4:
                    sourceBuilder.sort("sales", SortOrder.DESC);
                    break;
                default:
                    sourceBuilder.sort("_score", SortOrder.DESC);
                    break;
            }
        }


        // 3、构建分页条件
        Integer pageNum = paramVo.getPageNum();
        Integer pageSize = paramVo.getPageSize();
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);

        // 4、构建高亮
        sourceBuilder.highlighter(new HighlightBuilder().field("title").preTags("<font style='color:red;'>").postTags("</font>"));

        // 5、构建聚合
        // 5.1、品牌聚合
        sourceBuilder.aggregation(
                AggregationBuilders.terms("brandIdAgg").field("brandId")
                        .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                        .subAggregation(AggregationBuilders.terms("logoAgg").field("logo"))
        );

        // 5.2、分类聚合
        sourceBuilder.aggregation(
                AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                        .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName"))
        );

        // 5.3、规格参数的嵌套聚合
        sourceBuilder.aggregation(
                AggregationBuilders.nested("attrAgg", "searchAttrs")
                        .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                                .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                                .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue"))
                        )
        );

        // 6.结果集过滤
        sourceBuilder.fetchSource(new String[]{"skuId", "title", "subTitle", "price", "defaultImage"}, null);
        System.out.println("sourceBuilder = " + sourceBuilder);
        return sourceBuilder;
    }
}
