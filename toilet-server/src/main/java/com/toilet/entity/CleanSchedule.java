package com.toilet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tb_clean_schedule")
public class CleanSchedule {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull(message = "所属公厕不能为空")
    private Long toiletId;
    @NotNull(message = "保洁人员不能为空")
    private Long cleanerId;
    @NotNull(message = "排班日期不能为空")
    private LocalDate scheduleDate;
    @NotBlank(message = "班次类型不能为空")
    private String shiftType;
    @NotBlank(message = "保洁类型不能为空")
    private String cleanType;
    private String status;
    private LocalDateTime checkinTime;
    private LocalDateTime signoffTime;
    private Integer score;
    private String remark;
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
    private String cleanerName;
}
