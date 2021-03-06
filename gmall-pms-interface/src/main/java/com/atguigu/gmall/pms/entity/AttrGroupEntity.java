package com.atguigu.gmall.pms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 属性分组
 *
 * @author qmr
 * @email qmr@atguigu.com
 * @date 2020-09-21 18:48:44
 */
@Data
@TableName("pms_attr_group")
public class AttrGroupEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 分组id
     */
    @TableId
    private Long id;
    /**
     * 组名
     */
    private String name;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long categoryId;
    /**
     * 备注
     */
    private String remark;

    /**
     * 扩展字段，为了查询分类下的组及规格参数
     * 请求地址：/pms/attrgroup/withattrs/{catId}
     */
    @TableField(exist = false)
    private List<AttrEntity> attrEntities;
}
