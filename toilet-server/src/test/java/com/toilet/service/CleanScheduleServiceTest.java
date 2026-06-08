package com.toilet.service;

import com.toilet.dto.ScoreDTO;
import com.toilet.entity.CleanSchedule;
import com.toilet.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 保洁排班服务单元测试
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class CleanScheduleServiceTest {

    @Autowired
    private CleanScheduleService cleanScheduleService;

    @Test
    void testCheckin() {
        // 准备测试数据
        Long scheduleId = 1L;
        Long cleanerId = 1L;

        // 测试打卡
        assertDoesNotThrow(() -> cleanScheduleService.checkin(scheduleId, cleanerId));

        // 验证打卡时间
        CleanSchedule schedule = cleanScheduleService.getById(scheduleId);
        assertEquals("CHECKED_IN", schedule.getStatus());
        assertNotNull(schedule.getCheckinTime());

        System.out.println("CleanScheduleService - checkin 测试通过！");
    }

    @Test
    void testCheckinInvalidUser() {
        Long scheduleId = 1L;
        Long otherUserId = 2L; // 不同的保洁员ID

        // 尝试用错误的用户打卡
        assertThrows(BusinessException.class, () -> {
            cleanScheduleService.checkin(scheduleId, otherUserId);
        });

        System.out.println("CleanScheduleService - 无效打卡测试通过！");
    }

    @Test
    void testScore() {
        // scheduleId=2 is SIGNED_OFF status in test data
        Long scheduleId = 2L;

        // 测试评分
        ScoreDTO dto = new ScoreDTO();
        dto.setScheduleId(scheduleId);
        dto.setScore(95);
        dto.setRemark("评价良好");

        assertDoesNotThrow(() -> cleanScheduleService.score(scheduleId, 95, "评价良好"));

        // 验证评分和状态
        CleanSchedule schedule = cleanScheduleService.getById(scheduleId);
        assertEquals(95, schedule.getScore());
        assertEquals("评价良好", schedule.getRemark());
        assertEquals("FINISHED", schedule.getStatus());

        System.out.println("CleanScheduleService - score 测试通过！");
    }

    @Test
    void testScoreInvalidRange() {
        Long scheduleId = 1L;

        // 测试无效评分范围
        assertThrows(BusinessException.class, () -> {
            cleanScheduleService.score(scheduleId, -1, "测试");
        });

        assertThrows(BusinessException.class, () -> {
            cleanScheduleService.score(scheduleId, 101, "测试");
        });

        System.out.println("CleanScheduleService - 无效评分范围测试通过！");
    }

    @Test
    void testScoreInvalidSchedule() {
        // 测试不存在的排班记录
        assertThrows(BusinessException.class, () -> {
            cleanScheduleService.score(999L, 80, "测试");
        });

        System.out.println("CleanScheduleService - 不存在的排班记录测试通过！");
    }
}
