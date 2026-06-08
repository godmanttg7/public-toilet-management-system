package com.toilet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.toilet.entity.Feedback;
import com.toilet.entity.Message;
import com.toilet.entity.Toilet;
import com.toilet.exception.BusinessException;
import com.toilet.entity.User;
import com.toilet.mapper.FeedbackMapper;
import com.toilet.mapper.MessageMapper;
import com.toilet.mapper.ToiletMapper;
import com.toilet.mapper.UserMapper;
import com.toilet.service.FeedbackService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 反馈服务实现类
 */
@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    private static final int MIN_SCORE = 1;
    private static final int MAX_SCORE = 5;
    private static final int MAX_CONTENT_LENGTH = 500;
    private static final int MIN_CONTENT_LENGTH = 1;

    private final FeedbackMapper feedbackMapper;
    private final ToiletMapper toiletMapper;
    private final UserMapper userMapper;
    private final MessageMapper messageMapper;

    public FeedbackServiceImpl(FeedbackMapper feedbackMapper, ToiletMapper toiletMapper, UserMapper userMapper, MessageMapper messageMapper) {
        this.feedbackMapper = feedbackMapper;
        this.toiletMapper = toiletMapper;
        this.userMapper = userMapper;
        this.messageMapper = messageMapper;
    }

    @Override
    @Transactional
    public void submit(Feedback feedback, Long userId) {
        // 1. 校验评分范围
        if (feedback.getScore() == null || feedback.getScore() < MIN_SCORE || feedback.getScore() > MAX_SCORE) {
            throw BusinessException.badRequest("评分必须在" + MIN_SCORE + "-" + MAX_SCORE + "之间");
        }

        // 2. 校验内容长度
        if (StringUtils.isEmpty(feedback.getContent())) {
            throw BusinessException.badRequest("反馈内容不能为空");
        }
        if (feedback.getContent().length() < MIN_CONTENT_LENGTH || feedback.getContent().length() > MAX_CONTENT_LENGTH) {
            throw BusinessException.badRequest("反馈内容长度必须在" + MIN_CONTENT_LENGTH + "-" + MAX_CONTENT_LENGTH + "个字符之间");
        }

        // 3. 设置创建时间和用户ID
        feedback.setSubmitTime(LocalDateTime.now());
        if (userId != null) {
            feedback.setUserId(userId);
        }

        // 4. 保存反馈
        feedbackMapper.insert(feedback);

        // 5. 低分反馈(≤2) → 通知管理员
        if (feedback.getScore() <= 2) {
            String toiletName = "";
            if (feedback.getToiletId() != null) {
                Toilet t = toiletMapper.selectById(feedback.getToiletId());
                if (t != null) toiletName = "【" + t.getName() + "】";
            }
            String title = "低分反馈提醒";
            String content = String.format("%s收到%d分评价：%s", toiletName, feedback.getScore(), feedback.getContent());
            List<User> admins = userMapper.selectList(
                    new LambdaQueryWrapper<User>().eq(User::getRole, "ADMIN"));
            for (User admin : admins) {
                Message msg = new Message();
                msg.setUserId(admin.getId());
                msg.setTitle(title);
                msg.setContent(content);
                msg.setIsRead(0);
                messageMapper.insert(msg);
            }
        }
    }

    @Override
    public List<Feedback> getFeedbackByUser(Long userId) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getUserId, userId)
               .orderByDesc(Feedback::getSubmitTime);
        return feedbackMapper.selectList(wrapper);
    }

    @Override
    public List<Feedback> getFeedbackByToilet(Long toiletId) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getToiletId, toiletId)
               .orderByDesc(Feedback::getSubmitTime);
        return feedbackMapper.selectList(wrapper);
    }

    @Override
    public List<Feedback> getAllFeedback() {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Feedback::getSubmitTime);
        return feedbackMapper.selectList(wrapper);
    }

    @Override
    public void populateDisplay(List<Feedback> list) {
        if (list == null || list.isEmpty()) return;
        // 收集所有 toiletId
        List<Long> toiletIds = list.stream()
                .map(Feedback::getToiletId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (toiletIds.isEmpty()) return;
        // 批量查询公厕名称
        List<Toilet> toilets = toiletMapper.selectBatchIds(toiletIds);
        Map<Long, String> nameMap = toilets.stream()
                .collect(Collectors.toMap(Toilet::getId, Toilet::getName));
        // 填充公厕名称
        list.forEach(f -> {
            if (f.getToiletId() != null) {
                f.setToiletName(nameMap.get(f.getToiletId()));
            }
        });
        // 批量查询用户名称
        List<Long> userIds = list.stream()
                .map(Feedback::getUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            Map<Long, String> userNameMap = users.stream()
                    .collect(Collectors.toMap(User::getId, User::getRealName));
            list.forEach(f -> {
                if (f.getUserId() != null) {
                    f.setUserName(userNameMap.get(f.getUserId()));
                }
            });
        }
    }
}
