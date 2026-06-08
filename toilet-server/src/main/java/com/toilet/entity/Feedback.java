package com.toilet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@TableName("tb_feedback")
public class Feedback {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull(message = "所属公厕不能为空")
    private Long toiletId;
    @NotNull(message = "用户不能为空")
    private Long userId;
    @Min(value = 1, message = "评分最低为1分")
    @Max(value = 5, message = "评分最高为5分")
    private Integer score;
    @NotBlank(message = "反馈内容不能为空")
    @Size(min = 1, max = 500, message = "反馈内容长度必须在1-500个字符之间")
    private String content;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime submitTime;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    // 关联展示字段
    @TableField(exist = false)
    private String toiletName;
    @TableField(exist = false)
    private String userName;
}
