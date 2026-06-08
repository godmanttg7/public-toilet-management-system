package com.toilet.service;

import com.toilet.dto.DashboardStats;
import com.toilet.dto.ChartData;

import java.util.List;

public interface DashboardService {
    DashboardStats getStats();
    List<ChartData> getCleanRateTrend();
    List<ChartData> getFacilityRate();
    List<ChartData> getSatisfactionStats();
    List<ChartData> getConsumableUsage();
    List<ChartData> getLowStockAlerts();
}
