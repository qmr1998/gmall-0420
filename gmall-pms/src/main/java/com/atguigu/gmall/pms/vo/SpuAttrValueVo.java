package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author Lee
 * @date 2020-09-23  11:50
 */
@Data
public class SpuAttrValueVo extends SpuAttrValueEntity {

    /*
     baseAttrs对应的表是pms_spu_attr_value，对应的实体类是SpuAttrValueEntity

     此类对应添加spu时上传数据中的 baseAttrs，baseAttrs是个集合
     有attrId,attrName,valueSelected，让此类继承SpuAttrValueEntity，
     获得前两个属性，然后添加额外的valueSelected，
     valueSelected对应的是SpuAttrValueEntity中的attrValue，
     所以只需要重写set方法，将valueSelected的值设置给 attrValue，
     因为valueSelected是字符串集合，所以要将其拆分为字符串之后才能设置给attrValue */

    private List<String> valueSelected;

    public void setValueSelected(List<String> valueSelected) {
        // 如果接受的集合为空，则不设置
        if (CollectionUtils.isEmpty(valueSelected)) {
            return;
        }
        this.setAttrValue(StringUtils.join(valueSelected,","));

    }
}
