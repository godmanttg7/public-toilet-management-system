package com.toilet.vo;

import com.toilet.entity.Feedback;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
public class FeedbackVO {
    private Long id;
    private Long toiletId;
    private Long userId;
    private Integer score;
    private String content;
    private LocalDateTime submitTime;
    private String toiletName;
    private String userName;

    public static FeedbackVO fromEntity(Feedback feedback) {
        if (feedback == null) return null;
        FeedbackVO vo = new FeedbackVO();
        BeanUtils.copyProperties(feedback, vo);
        return vo;
    }
}
