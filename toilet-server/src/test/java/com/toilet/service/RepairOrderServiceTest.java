package com.toilet.service;

import com.toilet.dto.ChangeStatusDTO;
import com.toilet.dto.AssignDTO;
import com.toilet.entity.RepairOrder;
import com.toilet.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 报修工单服务单元测试
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class RepairOrderServiceTest {

    @Autowired
    private RepairOrderService repairOrderService;

    @Test
    void testAssignOrder() {
        // 准备测试数据
        Long orderId = 1L;
        Long assigneeId = 3L;

        // 测试指派工单
        assertDoesNotThrow(() -> repairOrderService.assignOrder(orderId, assigneeId));

        // 验证工单状态
        RepairOrder order = repairOrderService.getById(orderId);
        assertEquals("REPAIRING", order.getStatus());
        assertEquals(assigneeId, order.getAssigneeId());

        System.out.println("RepairOrderService - assignOrder 测试通过！");
    }

    @Test
    void testUpdateStatus() {
        Long orderId = 1L;
        Long assigneeId = 3L;

        // 先指派工单
        repairOrderService.assignOrder(orderId, assigneeId);

        // 测试状态更新为 CHECKING（维修人员完成维修）
        assertDoesNotThrow(() ->
            repairOrderService.updateStatus(orderId, "CHECKING", "维修完成，已更换配件", null, assigneeId, "REPAIR"));

        // 验证状态
        RepairOrder order = repairOrderService.getById(orderId);
        assertEquals("CHECKING", order.getStatus());

        // 测试状态更新为 FINISHED（管理员验收）
        repairOrderService.updateStatus(orderId, "FINISHED", null, null, 1L, "ADMIN");

        // 验证完成时间
        RepairOrder finishedOrder = repairOrderService.getById(orderId);
        assertEquals("FINISHED", finishedOrder.getStatus());
        assertNotNull(finishedOrder.getFinishTime());

        System.out.println("RepairOrderService - updateStatus 测试通过！");
    }

    @Test
    void testInvalidStatusUpdate() {
        Long orderId = 1L;

        // PENDING -> FINISHED 不允许，需要先指派
        assertThrows(BusinessException.class, () -> {
            repairOrderService.updateStatus(orderId, "FINISHED", null, null, 1L, "ADMIN");
        });

        System.out.println("RepairOrderService - 无效状态更新测试通过！");
    }

    @Test
    void testInvalidStatusTransition() {
        Long orderId = 1L;
        Long assigneeId = 3L;

        // 先指派工单
        repairOrderService.assignOrder(orderId, assigneeId);

        // REPAIRING -> FINISHED 不允许，必须经过 CHECKING
        assertThrows(BusinessException.class, () -> {
            repairOrderService.updateStatus(orderId, "FINISHED", null, null, assigneeId, "REPAIR");
        });

        System.out.println("RepairOrderService - 无效状态转换测试通过！");
    }

    @Test
    void testFullOrderFlow() {
        Long orderId = 1L;
        Long repairUserId = 3L;
        Long adminUserId = 1L;

        // 1. 指派工单
        repairOrderService.assignOrder(orderId, repairUserId);
        RepairOrder assigned = repairOrderService.getById(orderId);
        assertEquals("REPAIRING", assigned.getStatus());

        // 2. 维修完成，提交验收
        repairOrderService.updateStatus(orderId, "CHECKING", "已修复完成", null, repairUserId, "REPAIR");
        RepairOrder checking = repairOrderService.getById(orderId);
        assertEquals("CHECKING", checking.getStatus());

        // 3. 管理员验收通过
        repairOrderService.updateStatus(orderId, "FINISHED", null, null, adminUserId, "ADMIN");
        RepairOrder finished = repairOrderService.getById(orderId);
        assertEquals("FINISHED", finished.getStatus());
        assertNotNull(finished.getFinishTime());

        System.out.println("RepairOrderService - 完整工单流程测试通过！");
    }
}
