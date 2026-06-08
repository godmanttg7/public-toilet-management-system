package com.toilet.dto;

import lombok.Data;

@Data
public class DashboardStats {
    // 公厕统计
    private Long totalToilets;
    private Long activeToilets;

    // 用户统计
    private Long totalUsers;
    private Long activeUsers;
    private Long adminCount;
    private Long cleanerCount;
    private Long repairCount;

    // 工单统计
    private Long totalOrders;
    private Long pendingOrders;
    private Long repairingOrders;
    private Long checkingOrders;
    private Long finishedOrders;

    // 保洁统计
    private Long todaySchedules;
    private Long cleanSchedules;
    private Long cleanRate;

    // 反馈统计
    private Long feedbackCount;
    private Long pendingFeedbacks;

    // 耗材统计
    private Long lowStockCount;

    // 实时数据
    private Long todayOrders;
    private Long todayFeedbacks;
    private Long todaySchedulesCount;
}
