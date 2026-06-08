package com.toilet.service;

import com.toilet.entity.Feedback;
import com.toilet.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 反馈服务单元测试
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class FeedbackServiceTest {

    @Autowired
    private FeedbackService feedbackService;

    @Test
    void testFeedbackSubmitWithValidData() {
        // 准备测试数据
        Feedback feedback = new Feedback();
        feedback.setToiletId(1L);
        feedback.setUserId(1L);
        feedback.setScore(5);
        feedback.setContent("这是一个测试反馈内容");

        // 测试保存
        assertDoesNotThrow(() -> feedbackService.submit(feedback, 1L));

        // 测试评分范围验证
        Feedback invalidFeedback = new Feedback();
        invalidFeedback.setScore(6); // 超过最大值
        assertThrows(BusinessException.class, () -> {
            feedbackService.submit(invalidFeedback, 1L);
        });

        // 测试评分下限验证
        Feedback invalidFeedback2 = new Feedback();
        invalidFeedback2.setScore(0); // 低于最小值
        assertThrows(BusinessException.class, () -> {
            feedbackService.submit(invalidFeedback2, 1L);
        });

        System.out.println("FeedbackService 测试通过！");
    }

    @Test
    void testFeedbackContentValidation() {
        // 测试空内容
        Feedback emptyContent = new Feedback();
        emptyContent.setScore(5);
        emptyContent.setContent("");
        assertThrows(BusinessException.class, () -> {
            feedbackService.submit(emptyContent, 1L);
        });

        // 测试超长内容
        Feedback longContent = new Feedback();
        longContent.setScore(5);
        longContent.setContent("a".repeat(501)); // 超过最大长度
        assertThrows(BusinessException.class, () -> {
            feedbackService.submit(longContent, 1L);
        });

        System.out.println("Feedback内容验证测试通过！");
    }
}
