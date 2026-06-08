package com.toilet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.toilet.entity.Feedback;

import java.util.List;

public interface FeedbackService extends IService<Feedback> {
    /**
     * 提交反馈
     * @param feedback 反馈内容
     * @param userId 用户ID
     */
    void submit(Feedback feedback, Long userId);

    /**
     * 查询用户的所有反馈
     * @param userId 用户ID
     * @return 反馈列表
     */
    List<Feedback> getFeedbackByUser(Long userId);

    /**
     * 查询指定公厕的所有反馈
     * @param toiletId 公厕ID
     * @return 反馈列表
     */
    List<Feedback> getFeedbackByToilet(Long toiletId);

    /**
     * 查询所有反馈
     * @return 反馈列表
     */
    List<Feedback> getAllFeedback();

    /**
     * 填充关联展示字段（toiletName 等）
     */
    void populateDisplay(List<Feedback> list);
}
