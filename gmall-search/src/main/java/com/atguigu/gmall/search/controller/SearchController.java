package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Lee
 * @date 2020-09-27  21:07
 */
@Controller
@RequestMapping("search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    public String search(SearchParamVo searchParamVo, Model model) {
        SearchResponseVo responseVo = this.searchService.search(searchParamVo);
        model.addAttribute("searchParam", searchParamVo);
        model.addAttribute("response", responseVo);
        return "search";
//        return ResponseVo.ok(responseVo);
    }
}
