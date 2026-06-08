package com.toilet.controller;

import com.toilet.common.Result;
import com.toilet.dto.ChartData;
import com.toilet.dto.DashboardStats;
import com.toilet.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public Result<DashboardStats> getStats() {
        return Result.success(dashboardService.getStats());
    }

    @GetMapping("/clean-rate")
    public Result<List<ChartData>> getCleanRate() {
        return Result.success(dashboardService.getCleanRateTrend());
    }

    @GetMapping("/facility-rate")
    public Result<List<ChartData>> getFacilityRate() {
        return Result.success(dashboardService.getFacilityRate());
    }

    @GetMapping("/satisfaction")
    public Result<List<ChartData>> getSatisfaction() {
        return Result.success(dashboardService.getSatisfactionStats());
    }

    @GetMapping("/consumable-usage")
    public Result<List<ChartData>> getConsumableUsage() {
        return Result.success(dashboardService.getConsumableUsage());
    }

    @GetMapping("/alerts")
    public Result<List<ChartData>> getAlerts() {
        return Result.success(dashboardService.getLowStockAlerts());
    }
}
