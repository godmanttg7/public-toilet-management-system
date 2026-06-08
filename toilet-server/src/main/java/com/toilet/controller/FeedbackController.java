package com.toilet.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.toilet.common.Result;
import com.toilet.entity.Feedback;
import com.toilet.service.FeedbackService;
import com.toilet.vo.FeedbackVO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/page")
    public Result<IPage<FeedbackVO>> page(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          @RequestParam(required = false) Long toiletId) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(toiletId != null, Feedback::getToiletId, toiletId)
               .orderByDesc(Feedback::getSubmitTime);
        IPage<Feedback> page = feedbackService.page(new Page<>(current, size), wrapper);
        feedbackService.populateDisplay(page.getRecords());
        return Result.success(page.convert(FeedbackVO::fromEntity));
    }

    @PostMapping
    public Result<Void> submit(@Valid @RequestBody Feedback feedback, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        feedback.setUserId(userId);
        feedbackService.submit(feedback, userId);
        return Result.success();
    }
}
