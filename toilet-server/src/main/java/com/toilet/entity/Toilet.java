package com.toilet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_toilet")
public class Toilet {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "公厕名称不能为空")
    @Size(max = 100, message = "公厕名称不能超过100个字符")
    private String name;

    @NotBlank(message = "详细地址不能为空")
    @Size(max = 200, message = "详细地址不能超过200个字符")
    private String address;

    @NotNull(message = "经度不能为空")
    private BigDecimal longitude;

    @NotNull(message = "纬度不能为空")
    private BigDecimal latitude;

    @Size(max = 50, message = "所属区域不能超过50个字符")
    private String district;

    @Size(max = 50, message = "开放时间不能超过50个字符")
    private String openTime;

    private Integer maleCount;

    private Integer femaleCount;

    private Integer accessibleCount;

    @Size(max = 100, message = "管理单位不能超过100个字符")
    private String manageUnit;

    @Size(max = 20, message = "联系电话不能超过20个字符")
    private String phone;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
