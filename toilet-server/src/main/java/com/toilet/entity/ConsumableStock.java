package com.toilet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_consumable_stock")
public class ConsumableStock {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long toiletId;
    private String consumableName;
    private String unit;
    private Integer currentStock;
    private Integer minStock;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    // 关联展示字段
    @TableField(exist = false)
    private String toiletName;
}
