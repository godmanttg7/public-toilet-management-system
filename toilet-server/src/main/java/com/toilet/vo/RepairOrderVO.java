package com.toilet.vo;

import com.toilet.entity.RepairOrder;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
public class RepairOrderVO {
    private Long id;
    private String orderNo;
    private Long toiletId;
    private Long facilityId;
    private String faultDesc;
    private Long reporterId;
    private Long assigneeId;
    private String status;
    private String repairDesc;
    private String images;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
    private String toiletName;
    private String facilityType;
    private String reporterName;
    private String assigneeName;

    public static RepairOrderVO fromEntity(RepairOrder order) {
        if (order == null) return null;
        RepairOrderVO vo = new RepairOrderVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }
}
