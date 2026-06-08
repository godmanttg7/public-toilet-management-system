package com.toilet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.toilet.dto.ChartData;
import com.toilet.dto.DashboardStats;
import com.toilet.entity.*;
import com.toilet.mapper.*;
import com.toilet.service.DashboardService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final ToiletMapper toiletMapper;
    private final UserMapper userMapper;
    private final RepairOrderMapper repairOrderMapper;
    private final ConsumableStockMapper consumableStockMapper;
    private final CleanScheduleMapper cleanScheduleMapper;
    private final FacilityMapper facilityMapper;
    private final FeedbackMapper feedbackMapper;
    private final ConsumableRecordMapper consumableRecordMapper;

    public DashboardServiceImpl(ToiletMapper toiletMapper, UserMapper userMapper,
                                RepairOrderMapper repairOrderMapper,
                                ConsumableStockMapper consumableStockMapper,
                                CleanScheduleMapper cleanScheduleMapper,
                                FacilityMapper facilityMapper,
                                FeedbackMapper feedbackMapper,
                                ConsumableRecordMapper consumableRecordMapper) {
        this.toiletMapper = toiletMapper;
        this.userMapper = userMapper;
        this.repairOrderMapper = repairOrderMapper;
        this.consumableStockMapper = consumableStockMapper;
        this.cleanScheduleMapper = cleanScheduleMapper;
        this.facilityMapper = facilityMapper;
        this.feedbackMapper = feedbackMapper;
        this.consumableRecordMapper = consumableRecordMapper;
    }

    @Override
    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();

        // 公厕统计
        stats.setTotalToilets(toiletMapper.selectCount(null));
        stats.setActiveToilets(toiletMapper.selectCount(
                new LambdaQueryWrapper<Toilet>().eq(Toilet::getStatus, 1)));

        // 用户统计
        stats.setTotalUsers(userMapper.selectCount(null));
        stats.setActiveUsers(userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getStatus, 1)));
        stats.setAdminCount(userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRole, "ADMIN")));
        stats.setCleanerCount(userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRole, "CLEANER")));
        stats.setRepairCount(userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRole, "REPAIR")));

        // 工单统计
        stats.setTotalOrders(repairOrderMapper.selectCount(null));
        stats.setPendingOrders(repairOrderMapper.selectCount(
                new LambdaQueryWrapper<RepairOrder>().eq(RepairOrder::getStatus, "PENDING")));
        stats.setRepairingOrders(repairOrderMapper.selectCount(
                new LambdaQueryWrapper<RepairOrder>().eq(RepairOrder::getStatus, "REPAIRING")));
        stats.setCheckingOrders(repairOrderMapper.selectCount(
                new LambdaQueryWrapper<RepairOrder>().eq(RepairOrder::getStatus, "CHECKING")));
        stats.setFinishedOrders(repairOrderMapper.selectCount(
                new LambdaQueryWrapper<RepairOrder>().eq(RepairOrder::getStatus, "FINISHED")));

        // 保洁统计
        LocalDate today = LocalDate.now();
        stats.setTodaySchedules(cleanScheduleMapper.selectCount(
                new LambdaQueryWrapper<CleanSchedule>().eq(CleanSchedule::getScheduleDate, today)));
        stats.setCleanSchedules(cleanScheduleMapper.selectCount(
                new LambdaQueryWrapper<CleanSchedule>()
                        .eq(CleanSchedule::getStatus, "FINISHED")
                        .eq(CleanSchedule::getScheduleDate, today)));
        long totalToday = stats.getTodaySchedules();
        if (totalToday > 0) {
            stats.setCleanRate(stats.getCleanSchedules() * 100L / totalToday);
        } else {
            stats.setCleanRate(0L);
        }

        // 反馈统计
        stats.setFeedbackCount(feedbackMapper.selectCount(null));
        // 反馈表无状态字段，暂不统计待处理数
        stats.setPendingFeedbacks(0L);

        // 耗材统计
        List<ConsumableStock> stocks = consumableStockMapper.selectList(null);
        long lowStockCount = stocks.stream()
                .filter(s -> s.getCurrentStock() < s.getMinStock())
                .count();
        stats.setLowStockCount(lowStockCount);

        // 实时统计
        stats.setTodayOrders(repairOrderMapper.selectCount(
                new LambdaQueryWrapper<RepairOrder>()
                        .ge(RepairOrder::getCreateTime, LocalDateTime.now().toLocalDate().atStartOfDay())));
        stats.setTodayFeedbacks(feedbackMapper.selectCount(
                new LambdaQueryWrapper<Feedback>()
                        .ge(Feedback::getSubmitTime, LocalDateTime.now().toLocalDate().atStartOfDay())));

        return stats;
    }

    @Override
    public List<ChartData> getCleanRateTrend() {
        List<CleanSchedule> schedules = cleanScheduleMapper.selectList(
                new LambdaQueryWrapper<CleanSchedule>().isNotNull(CleanSchedule::getScore));
        Map<Long, List<CleanSchedule>> grouped = schedules.stream()
                .collect(Collectors.groupingBy(CleanSchedule::getToiletId));

        // 获取公厕名称映射
        Map<Long, String> toiletNames = getToiletNameMap(grouped.keySet());

        List<ChartData> result = new ArrayList<>();
        for (Map.Entry<Long, List<CleanSchedule>> entry : grouped.entrySet()) {
            List<CleanSchedule> list = entry.getValue();
            long passCount = list.stream().filter(s -> s.getScore() >= 60).count();
            BigDecimal rate = BigDecimal.valueOf(passCount * 100.0 / list.size())
                    .setScale(1, RoundingMode.HALF_UP);
            ChartData cd = new ChartData();
            cd.setName(toiletNames.getOrDefault(entry.getKey(), "公厕#" + entry.getKey()));
            cd.setValue(rate);
            result.add(cd);
        }
        return result;
    }

    private Map<Long, String> getToiletNameMap(java.util.Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return java.util.Collections.emptyMap();
        return toiletMapper.selectBatchIds(new java.util.ArrayList<>(ids)).stream()
                .collect(Collectors.toMap(Toilet::getId, Toilet::getName));
    }

    @Override
    public List<ChartData> getFacilityRate() {
        List<Facility> facilities = facilityMapper.selectList(null);
        Map<Long, List<Facility>> grouped = facilities.stream()
                .collect(Collectors.groupingBy(Facility::getToiletId));

        Map<Long, String> toiletNames = getToiletNameMap(grouped.keySet());

        List<ChartData> result = new ArrayList<>();
        for (Map.Entry<Long, List<Facility>> entry : grouped.entrySet()) {
            List<Facility> list = entry.getValue();
            long normalCount = list.stream().filter(f -> "NORMAL".equals(f.getStatus())).count();
            BigDecimal rate = BigDecimal.valueOf(normalCount * 100.0 / list.size())
                    .setScale(1, RoundingMode.HALF_UP);
            ChartData cd = new ChartData();
            cd.setName(toiletNames.getOrDefault(entry.getKey(), "公厕#" + entry.getKey()));
            cd.setValue(rate);
            result.add(cd);
        }
        return result;
    }

    @Override
    public List<ChartData> getSatisfactionStats() {
        List<Feedback> feedbacks = feedbackMapper.selectList(null);
        // 过滤 toiletId 为 null 的记录（公众展示台反馈无公厕上下文）
        Map<Long, List<Feedback>> grouped = feedbacks.stream()
                .filter(f -> f.getToiletId() != null)
                .collect(Collectors.groupingBy(Feedback::getToiletId));

        Map<Long, String> toiletNames = getToiletNameMap(grouped.keySet());

        List<ChartData> result = new ArrayList<>();
        for (Map.Entry<Long, List<Feedback>> entry : grouped.entrySet()) {
            double avg = entry.getValue().stream().mapToInt(Feedback::getScore).average().orElse(0);
            ChartData cd = new ChartData();
            cd.setName(toiletNames.getOrDefault(entry.getKey(), "公厕#" + entry.getKey()));
            cd.setValue(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
            result.add(cd);
        }
        return result;
    }

    @Override
    public List<ChartData> getConsumableUsage() {
        List<ConsumableRecord> records = consumableRecordMapper.selectList(
                new LambdaQueryWrapper<ConsumableRecord>()
                        .eq(ConsumableRecord::getType, "OUT")
                        .ge(ConsumableRecord::getCreateTime, LocalDateTime.now().minusDays(30)));

        Map<String, Integer> grouped = records.stream()
                .collect(Collectors.groupingBy(ConsumableRecord::getConsumableName,
                        Collectors.summingInt(ConsumableRecord::getQuantity)));

        List<ChartData> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : grouped.entrySet()) {
            ChartData cd = new ChartData();
            cd.setName(entry.getKey());
            cd.setValue(BigDecimal.valueOf(entry.getValue()));
            result.add(cd);
        }
        return result;
    }

    @Override
    public List<ChartData> getLowStockAlerts() {
        List<ConsumableStock> stocks = consumableStockMapper.selectList(null);
        List<ChartData> result = new ArrayList<>();
        for (ConsumableStock s : stocks) {
            if (s.getCurrentStock() < s.getMinStock()) {
                ChartData cd = new ChartData();
                cd.setName(s.getConsumableName() + "(公厕ID:" + s.getToiletId() + ")");
                cd.setValue(BigDecimal.valueOf(s.getCurrentStock()));
                result.add(cd);
            }
        }
        return result;
    }
}
