package com.toilet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@TableName("tb_repair_order")
public class RepairOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    @NotNull(message = "所属公厕不能为空")
    private Long toiletId;
    @NotNull(message = "故障设施不能为空")
    private Long facilityId;
    @NotBlank(message = "故障描述不能为空")
    private String faultDesc;
    @NotNull(message = "报告人不能为空")
    private Long reporterId;
    private Long assigneeId;
    private String status;
    private String repairDesc;
    private String images;
    private String reporterName;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private LocalDateTime finishTime;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    // 关联展示字段
    @TableField(exist = false)
    private String toiletName;
    @TableField(exist = false)
    private String facilityType;
    @TableField(exist = false)
    private String assigneeName;
}
