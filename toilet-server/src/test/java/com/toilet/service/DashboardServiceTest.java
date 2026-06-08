package com.toilet.service;

import com.toilet.dto.ChartData;
import com.toilet.dto.DashboardStats;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class DashboardServiceTest {

    @Autowired
    private DashboardService dashboardService;

    @Test
    void testGetStats() {
        DashboardStats stats = dashboardService.getStats();

        // 公厕统计
        assertEquals(2L, stats.getTotalToilets());
        assertEquals(2L, stats.getActiveToilets());

        // 用户统计（admin + cleaner1 + repair1 + disabled）
        assertEquals(4L, stats.getTotalUsers());
        assertEquals(3L, stats.getActiveUsers()); // disabled 状态为0
        assertEquals(1L, stats.getAdminCount());
        assertEquals(2L, stats.getCleanerCount()); // cleaner1 + disabled
        assertEquals(1L, stats.getRepairCount());

        // 工单统计
        assertEquals(1L, stats.getTotalOrders());
        assertEquals(1L, stats.getPendingOrders());
        assertEquals(0L, stats.getRepairingOrders());
        assertEquals(0L, stats.getCheckingOrders());
        assertEquals(0L, stats.getFinishedOrders());

        // 耗材低库存告警（洗手液 current=5 < min=10）
        assertEquals(1L, stats.getLowStockCount());

        System.out.println("DashboardService - getStats 测试通过！");
    }

    @Test
    void testGetCleanRateTrend() {
        List<ChartData> data = dashboardService.getCleanRateTrend();
        // 有评分的排班: schedule3(toilet1, 85), schedule4(toilet2, 45)
        // toilet1: 1/1=100%; toilet2: 0/1=0%
        assertFalse(data.isEmpty());

        for (ChartData cd : data) {
            if (cd.getName().contains("公厕ID:1")) {
                assertEquals(0, BigDecimal.valueOf(100.0).compareTo(cd.getValue()));
            }
        }

        System.out.println("DashboardService - getCleanRateTrend 测试通过！");
    }

    @Test
    void testGetFacilityRate() {
        List<ChartData> data = dashboardService.getFacilityRate();
        // facility1(toilet1, NORMAL), facility2(toilet1, BROKEN), facility3(toilet2, NORMAL)
        // toilet1: 1/2=50%; toilet2: 1/1=100%
        assertFalse(data.isEmpty());

        for (ChartData cd : data) {
            if (cd.getName().contains("公厕ID:1")) {
                assertEquals(0, BigDecimal.valueOf(50.0).compareTo(cd.getValue()));
            }
        }

        System.out.println("DashboardService - getFacilityRate 测试通过！");
    }

    @Test
    void testGetSatisfactionStats() {
        List<ChartData> data = dashboardService.getSatisfactionStats();
        // feedback1(toilet1, 5), feedback2(toilet1, 4), feedback4(toilet1, 2) = avg 3.7
        // feedback3(toilet2, 3) = avg 3.0
        assertFalse(data.isEmpty());

        System.out.println("DashboardService - getSatisfactionStats 测试通过！");
    }

    @Test
    void testGetConsumableUsage() {
        List<ChartData> data = dashboardService.getConsumableUsage();
        // 一条 OUT 记录: 卫生纸 10
        assertFalse(data.isEmpty());

        System.out.println("DashboardService - getConsumableUsage 测试通过！");
    }

    @Test
    void testGetLowStockAlerts() {
        List<ChartData> data = dashboardService.getLowStockAlerts();
        // stock2(洗手液, current=5 < min=10)
        assertEquals(1, data.size());
        assertTrue(data.get(0).getName().contains("洗手液"));

        System.out.println("DashboardService - getLowStockAlerts 测试通过！");
    }
}
