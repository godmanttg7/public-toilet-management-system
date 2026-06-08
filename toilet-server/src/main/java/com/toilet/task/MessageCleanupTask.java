package com.toilet.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.toilet.entity.Message;
import com.toilet.mapper.MessageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Component
public class MessageCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(MessageCleanupTask.class);
    private static final int RETENTION_DAYS = 30;

    @Resource
    private MessageMapper messageMapper;

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOldMessages() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(RETENTION_DAYS);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(Message::getCreateTime, deadline);
        int deleted = messageMapper.delete(wrapper);
        if (deleted > 0) {
            log.info("已清理 {} 条过期消息（保留{}天）", deleted, RETENTION_DAYS);
        }
    }
}
