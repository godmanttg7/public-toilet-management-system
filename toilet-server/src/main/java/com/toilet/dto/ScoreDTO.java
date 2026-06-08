package com.toilet.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

@Data
public class ScoreDTO {
    @NotNull(message = "排班ID不能为空")
    private Long scheduleId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1分")
    @Max(value = 100, message = "评分最高为100分")
    private Integer score;

    private String remark;
}
