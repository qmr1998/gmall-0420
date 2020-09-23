package com.atguigu.gmall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.wms.entity.PurchaseEntity;

import java.util.Map;

/**
 * 采购信息
 *
 * @author qmr
 * @email qmr@atguigu.com
 * @date 2020-09-22 19:43:42
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

