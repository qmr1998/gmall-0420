package com.eatguigu.gmall.item.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.eatguigu.gmall.item.service.ItemService;
import com.eatguigu.gmall.item.vo.ItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Lee
 * @date 2020-10-12  20:37
 */
@RestController
@RequestMapping("item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping("{skuId}.html")
    public ResponseVo<ItemVo> load(@PathVariable("skuId")Long skuId){

        ItemVo itemVo = this.itemService.load(skuId);

        return ResponseVo.ok(itemVo);
    }

}
