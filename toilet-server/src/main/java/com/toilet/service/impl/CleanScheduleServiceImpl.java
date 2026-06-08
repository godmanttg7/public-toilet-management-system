package com.toilet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.toilet.entity.CleanSchedule;
import com.toilet.entity.Message;
import com.toilet.entity.Toilet;
import com.toilet.entity.User;
import com.toilet.exception.BusinessException;
import com.toilet.mapper.CleanScheduleMapper;
import com.toilet.mapper.MessageMapper;
import com.toilet.mapper.ToiletMapper;
import com.toilet.mapper.UserMapper;
import com.toilet.service.CleanScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CleanScheduleServiceImpl extends ServiceImpl<CleanScheduleMapper, CleanSchedule>
        implements CleanScheduleService {

    private final CleanScheduleMapper cleanScheduleMapper;
    private final ToiletMapper toiletMapper;
    private final UserMapper userMapper;
    private final MessageMapper messageMapper;

    public CleanScheduleServiceImpl(CleanScheduleMapper cleanScheduleMapper,
                                    ToiletMapper toiletMapper,
                                    UserMapper userMapper,
                                    MessageMapper messageMapper) {
        this.cleanScheduleMapper = cleanScheduleMapper;
        this.toiletMapper = toiletMapper;
        this.userMapper = userMapper;
        this.messageMapper = messageMapper;
    }

    @Override
    @Transactional
    public void saveSchedule(CleanSchedule schedule) {
        // 校验公厕是否存在
        Toilet toilet = toiletMapper.selectById(schedule.getToiletId());
        if (toilet == null) {
            throw BusinessException.badRequest("公厕不存在，请重新选择");
        }
        // 校验保洁员是否存在且角色为CLEANER
        User cleaner = userMapper.selectById(schedule.getCleanerId());
        if (cleaner == null) {
            throw BusinessException.badRequest("保洁员不存在，请重新选择");
        }
        if (!"CLEANER".equals(cleaner.getRole())) {
            throw BusinessException.badRequest("所选用户不是保洁人员");
        }
        // 冲突检测：同人同天同班次不可重复
        Long exists = cleanScheduleMapper.selectCount(new LambdaQueryWrapper<CleanSchedule>()
                .eq(CleanSchedule::getCleanerId, schedule.getCleanerId())
                .eq(CleanSchedule::getScheduleDate, schedule.getScheduleDate())
                .eq(CleanSchedule::getShiftType, schedule.getShiftType())
                .ne(schedule.getId() != null, CleanSchedule::getId, schedule.getId()));
        if (exists > 0) {
            throw BusinessException.badRequest("该保洁员在此时间已有排班，请调整");
        }
        schedule.setStatus("PENDING");
        super.save(schedule);
    }

    @Override
    @Transactional
    public void checkin(Long scheduleId, Long cleanerId) {
        CleanSchedule schedule = cleanScheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw BusinessException.badRequest("排班记录不存在");
        }
        if (!cleanerId.equals(schedule.getCleanerId())) {
            throw BusinessException.badRequest("该任务不属于当前用户");
        }
        if (!"PENDING".equals(schedule.getStatus())) {
            throw BusinessException.badRequest("当前排班状态不允许打卡");
        }
        schedule.setStatus("CHECKED_IN");
        schedule.setCheckinTime(LocalDateTime.now());
        cleanScheduleMapper.updateById(schedule);
    }

    @Override
    @Transactional
    public void signoff(Long scheduleId, Long cleanerId) {
        CleanSchedule schedule = cleanScheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw BusinessException.badRequest("排班记录不存在");
        }
        if (!cleanerId.equals(schedule.getCleanerId())) {
            throw BusinessException.badRequest("该任务不属于当前用户");
        }
        if (!"CHECKED_IN".equals(schedule.getStatus())) {
            throw BusinessException.badRequest("仅已打卡状态的排班可以签退");
        }
        schedule.setStatus("SIGNED_OFF");
        schedule.setSignoffTime(LocalDateTime.now());
        cleanScheduleMapper.updateById(schedule);
    }

    @Override
    @Transactional
    public void score(Long scheduleId, Integer score, String remark) {
        CleanSchedule schedule = cleanScheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw BusinessException.badRequest("排班记录不存在");
        }
        if (!"SIGNED_OFF".equals(schedule.getStatus()) && !"REJECTED".equals(schedule.getStatus())) {
            throw BusinessException.badRequest("仅已签退或被退回的排班可以评分");
        }
        if (score < 0 || score > 100) {
            throw BusinessException.badRequest("评分需在0-100之间");
        }
        schedule.setScore(score);
        schedule.setRemark(remark);
        if (score >= 60) {
            schedule.setStatus("FINISHED");
        } else {
            // 评分<60：退回整改，重新作业
            schedule.setStatus("REJECTED");
            // 通知保洁员整改
            String toiletName = "";
            Toilet t = toiletMapper.selectById(schedule.getToiletId());
            if (t != null) toiletName = "【" + t.getName() + "】";
            String title = "保洁整改通知";
            String content = String.format("%s%s的保洁评分%d分，未达标，请及时整改。原因：%s",
                    toiletName, schedule.getScheduleDate(), score,
                    remark != null ? remark : "未达标");
            Message msg = new Message();
            msg.setUserId(schedule.getCleanerId());
            msg.setTitle(title);
            msg.setContent(content);
            msg.setIsRead(0);
            messageMapper.insert(msg);
        }
        cleanScheduleMapper.updateById(schedule);
    }

    @Override
    public void populateDisplay(List<CleanSchedule> list) {
        if (list == null || list.isEmpty()) return;
        // 批量查询公厕
        List<Long> toiletIds = list.stream().map(CleanSchedule::getToiletId).filter(id -> id != null).distinct().collect(Collectors.toList());
        Map<Long, String> toiletMap = toiletMapper.selectBatchIds(toiletIds).stream().collect(Collectors.toMap(Toilet::getId, Toilet::getName));
        // 批量查询保洁员
        List<Long> userIds = list.stream().map(CleanSchedule::getCleanerId).filter(id -> id != null).distinct().collect(Collectors.toList());
        Map<Long, String> userMap = userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, User::getRealName));
        // 填充
        list.forEach(s -> {
            if (s.getToiletId() != null) s.setToiletName(toiletMap.get(s.getToiletId()));
            if (s.getCleanerId() != null) s.setCleanerName(userMap.get(s.getCleanerId()));
        });
    }
}
