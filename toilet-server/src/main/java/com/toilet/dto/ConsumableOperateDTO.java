package com.toilet.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;

@Data
public class ConsumableOperateDTO {
    @NotNull(message = "公厕ID不能为空")
    private Long toiletId;

    @NotNull(message = "耗材名称不能为空")
    private String consumableName;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;
}
