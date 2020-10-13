package com.eatguigu.gmall.item.service;

import com.eatguigu.gmall.item.vo.ItemVo;

/**
 * @author Lee
 * @date 2020-10-12  20:37
 */
public interface ItemService {
    ItemVo load(Long skuId);
}
