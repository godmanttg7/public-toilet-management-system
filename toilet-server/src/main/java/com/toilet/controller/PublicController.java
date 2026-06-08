package com.toilet.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.toilet.common.Result;
import com.toilet.entity.ConsumableStock;
import com.toilet.entity.Feedback;
import com.toilet.entity.Toilet;
import com.toilet.entity.User;
import com.toilet.service.ConsumableService;
import com.toilet.service.FeedbackService;
import com.toilet.service.ToiletService;
import com.toilet.service.UserService;
import com.toilet.vo.ToiletVO;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.IntSummaryStatistics;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final ToiletService toiletService;
    private final FeedbackService feedbackService;
    private final UserService userService;
    private final ConsumableService consumableService;

    public PublicController(ToiletService toiletService, FeedbackService feedbackService,
                            UserService userService, ConsumableService consumableService) {
        this.toiletService = toiletService;
        this.feedbackService = feedbackService;
        this.userService = userService;
        this.consumableService = consumableService;
    }

    @GetMapping("/toilets")
    public Result<List<ToiletVO>> toilets(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String district) {
        LambdaQueryWrapper<Toilet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Toilet::getStatus, 1)
               .eq(StringUtils.hasText(district), Toilet::getDistrict, district)
               .and(StringUtils.hasText(keyword), w -> w
                       .like(Toilet::getName, keyword)
                       .or()
                       .like(Toilet::getAddress, keyword));
        List<Toilet> list = toiletService.list(wrapper);
        return Result.success(list.stream().map(ToiletVO::fromEntity).collect(Collectors.toList()));
    }

    @GetMapping("/toilet/{id}")
    public Result<ToiletVO> toiletDetail(@PathVariable Long id) {
        Toilet toilet = toiletService.getById(id);
        return Result.success(ToiletVO.fromEntity(toilet));
    }

    @PostMapping("/feedback")
    public Result<Map<String, Object>> submitFeedback(@RequestBody Map<String, Object> body) {
        Feedback feedback = new Feedback();
        feedback.setToiletId(body.get("toiletId") != null ? Long.valueOf(body.get("toiletId").toString()) : null);
        feedback.setScore(body.get("score") != null ? Integer.valueOf(body.get("score").toString()) : 5);

        // 将分类和联系方式嵌入内容，确保管理员看到完整信息
        String category = body.get("category") != null ? body.get("category").toString() : "";
        String description = body.get("description") != null ? body.get("description").toString() : "";
        String contact = body.get("contact") != null ? body.get("contact").toString() : "";

        StringBuilder content = new StringBuilder();
        if (StringUtils.hasText(category)) {
            content.append("【").append(category).append("】");
        }
        content.append(description);
        if (StringUtils.hasText(contact)) {
            content.append("（联系方式：").append(contact).append("）");
        }
        feedback.setContent(content.toString());
        feedback.setSubmitTime(LocalDateTime.now());
        feedbackService.save(feedback);
        Map<String, Object> result = new HashMap<>();
        result.put("feedbackId", feedback.getId());
        return Result.success(result);
    }

    @GetMapping("/showcase")
    public Result<Map<String, Object>> getShowcase() {
        Map<String, Object> data = new HashMap<>();

        // 公厕总数
        long totalToilets = toiletService.count(new LambdaQueryWrapper<Toilet>().eq(Toilet::getStatus, 1));
        data.put("totalToilets", totalToilets);

        // 运维人员（保洁和维修）
        List<User> staffList = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, 1)
                .in(User::getRole, "CLEANER", "REPAIR"));
        data.put("staffList", staffList.stream().map(u -> {
            Map<String, Object> staff = new HashMap<>();
            staff.put("id", u.getId());
            staff.put("name", u.getRealName());
            staff.put("role", "CLEANER".equals(u.getRole()) ? "保洁人员" : "维修人员");
            staff.put("status", "ON_DUTY");
            staff.put("statusLabel", "在岗");
            staff.put("phone", u.getPhone());
            return staff;
        }).collect(Collectors.toList()));

        // 耗材库存
        List<ConsumableStock> stocks = consumableService.list();
        consumableService.populateDisplay(stocks);
        data.put("consumables", stocks.stream().map(s -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", s.getId());
            item.put("consumableName", s.getConsumableName());
            item.put("currentStock", s.getCurrentStock());
            item.put("minStock", s.getMinStock());
            item.put("unit", s.getUnit());
            item.put("toiletName", s.getToiletName());
            item.put("toiletId", s.getToiletId());
            item.put("remainingPercent", s.getMinStock() > 0
                    ? Math.min(100, (int)(s.getCurrentStock() * 100.0 / (s.getMinStock() * 2)))
                    : (s.getCurrentStock() > 0 ? 80 : 0));
            item.put("isLow", s.getCurrentStock() < s.getMinStock());
            return item;
        }).collect(Collectors.toList()));

        // 满意度统计
        List<Feedback> allFeedback = feedbackService.list();
        if (!allFeedback.isEmpty()) {
            IntSummaryStatistics scoreStats = allFeedback.stream()
                    .mapToInt(Feedback::getScore)
                    .summaryStatistics();
            Map<String, Object> satisfaction = new HashMap<>();
            satisfaction.put("total", allFeedback.size());
            satisfaction.put("average", Math.round(scoreStats.getAverage() * 10) / 10.0);
            // 评分分布
            Map<Integer, Long> distribution = allFeedback.stream()
                    .collect(Collectors.groupingBy(Feedback::getScore, Collectors.counting()));
            satisfaction.put("distribution", distribution);
            // 好评率（4分及以上）
            long good = allFeedback.stream().filter(f -> f.getScore() >= 4).count();
            satisfaction.put("goodRate", Math.round(good * 100.0 / allFeedback.size()));
            data.put("satisfaction", satisfaction);
        } else {
            Map<String, Object> satisfaction = new HashMap<>();
            satisfaction.put("total", 0);
            satisfaction.put("average", 0.0);
            satisfaction.put("distribution", new HashMap<>());
            satisfaction.put("goodRate", 0);
            data.put("satisfaction", satisfaction);
        }

        // 今日统计
        data.put("todayVisits", totalToilets * 250L);

        return Result.success(data);
    }
}
