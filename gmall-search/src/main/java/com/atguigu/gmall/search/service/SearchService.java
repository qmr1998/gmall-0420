package com.atguigu.gmall.search.service;

import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;

/**
 * @author Lee
 * @date 2020-09-27  21:08
 */
public interface SearchService {
    SearchResponseVo search(SearchParamVo searchParamVo);
}
