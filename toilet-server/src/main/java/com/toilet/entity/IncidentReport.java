package com.toilet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_incident_report")
public class IncidentReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long toiletId;
    private Long reporterId;
    private String reporterRole;
    private String reportType;
    private String description;
    private String images;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String toiletName;
    @TableField(exist = false)
    private String reporterName;
}
