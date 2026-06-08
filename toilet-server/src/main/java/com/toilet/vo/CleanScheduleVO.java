package com.toilet.vo;

import com.toilet.entity.CleanSchedule;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CleanScheduleVO {
    private Long id;
    private Long toiletId;
    private Long cleanerId;
    private LocalDate scheduleDate;
    private String shiftType;
    private String cleanType;
    private String status;
    private LocalDateTime checkinTime;
    private LocalDateTime signoffTime;
    private Integer score;
    private String remark;
    private LocalDateTime createTime;
    private String toiletName;
    private String cleanerName;

    public static CleanScheduleVO fromEntity(CleanSchedule schedule) {
        if (schedule == null) return null;
        CleanScheduleVO vo = new CleanScheduleVO();
        BeanUtils.copyProperties(schedule, vo);
        return vo;
    }
}
