package com.toilet.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AssignDTO {
    @NotNull(message = "工单ID不能为空")
    private Long orderId;

    @NotNull(message = "指派人员ID不能为空")
    private Long assigneeId;
}
