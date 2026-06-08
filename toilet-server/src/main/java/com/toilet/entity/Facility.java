package com.toilet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tb_facility")
public class Facility {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull(message = "所属公厕不能为空")
    private Long toiletId;
    @NotBlank(message = "设施类型不能为空")
    private String facilityType;
    private String brand;
    @TableField("\"model\"")
    private String model;
    private LocalDate installDate;
    private LocalDate warrantyDate;
    private String status;
    @Version
    private Integer version;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    // 关联展示字段
    @TableField(exist = false)
    private String toiletName;
}
