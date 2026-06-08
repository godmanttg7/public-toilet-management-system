package com.toilet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.toilet.entity.CleanSchedule;

import java.util.List;

public interface CleanScheduleService extends IService<CleanSchedule> {
    void saveSchedule(CleanSchedule schedule);
    void checkin(Long scheduleId, Long cleanerId);
    void signoff(Long scheduleId, Long cleanerId);
    void score(Long scheduleId, Integer score, String remark);
    void populateDisplay(List<CleanSchedule> list);
}
