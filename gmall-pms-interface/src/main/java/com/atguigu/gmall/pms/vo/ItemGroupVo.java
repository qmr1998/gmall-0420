package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.vo.AttrValueVo;
import lombok.Data;

import java.util.List;

/**
 * @author Lee
 * @date 2020-10-12  18:06
 */
// 规格参数组及组下的规格参数(带值)
@Data
public class ItemGroupVo {

    private Long id;
    private String groupName;
    private List<AttrValueVo> attrValues;

}
