package com.toilet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.toilet.entity.RepairOrder;

import java.util.List;

public interface RepairOrderService extends IService<RepairOrder> {
    void updateStatus(Long orderId, String newStatus, String repairDesc, String images, Long userId, String role);
    void assignOrder(Long orderId, Long assigneeId);
    void cancelOrder(Long orderId);
    void populateDisplay(List<RepairOrder> list);
}
