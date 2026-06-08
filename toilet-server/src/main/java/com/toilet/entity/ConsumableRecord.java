package com.toilet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_consumable_record")
public class ConsumableRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long toiletId;
    private String consumableName;
    private Integer quantity;
    private String type;
    private Long operatorId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    // 关联展示字段
    @TableField(exist = false)
    private String toiletName;
    @TableField(exist = false)
    private String operatorName;
}
