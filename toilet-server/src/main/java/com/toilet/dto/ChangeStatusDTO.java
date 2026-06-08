package com.toilet.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class ChangeStatusDTO {
    @NotNull(message = "工单ID不能为空")
    private Long orderId;

    @NotNull(message = "状态不能为空")
    @Pattern(regexp = "^(PENDING|REPAIRING|CHECKING|FINISHED|CANCELLED)$", message = "工单状态值不合法")
    private String newStatus;

    /** 维修描述（维修完成时填写） */
    private String repairDesc;

    /** 维修照片URL（多个用逗号分隔） */
    private String images;
}
