package com.toilet.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.toilet.common.Result;
import com.toilet.entity.Message;
import com.toilet.service.MessageService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/page")
    public Result<IPage<Message>> page(@RequestParam(defaultValue = "1") Integer current,
                                        @RequestParam(defaultValue = "10") Integer size,
                                        Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId)
                .orderByDesc(Message::getCreateTime);
        return Result.success(messageService.page(new Page<>(current, size), wrapper));
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        long count = messageService.count(new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0));
        return Result.success(count);
    }

    @PutMapping("/read/{id}")
    public Result<Void> markRead(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        Message msg = messageService.getById(id);
        if (msg != null && msg.getUserId().equals(userId)) {
            msg.setIsRead(1);
            messageService.updateById(msg);
        }
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result<Void> markAllRead(Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        messageService.lambdaUpdate()
                .eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0)
                .set(Message::getIsRead, 1)
                .update();
        return Result.success();
    }
}
