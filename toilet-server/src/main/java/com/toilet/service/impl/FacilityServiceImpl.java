package com.toilet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.toilet.common.DisplayUtils;
import com.toilet.entity.Facility;
import com.toilet.entity.RepairOrder;
import com.toilet.exception.BusinessException;
import com.toilet.mapper.FacilityMapper;
import com.toilet.mapper.RepairOrderMapper;
import com.toilet.mapper.ToiletMapper;
import com.toilet.service.FacilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FacilityServiceImpl extends ServiceImpl<FacilityMapper, Facility> implements FacilityService {

    private static final Logger log = LoggerFactory.getLogger(FacilityServiceImpl.class);
    private static final int MAX_RETRIES = 3;

    private final FacilityMapper facilityMapper;
    private final ToiletMapper toiletMapper;
    private final RepairOrderMapper repairOrderMapper;

    public FacilityServiceImpl(FacilityMapper facilityMapper, ToiletMapper toiletMapper,
                               RepairOrderMapper repairOrderMapper) {
        super();
        this.facilityMapper = facilityMapper;
        this.toiletMapper = toiletMapper;
        this.repairOrderMapper = repairOrderMapper;
    }

    @Override
    public void populateDisplay(List<Facility> list) {
        DisplayUtils.populateToiletName(list,
                Facility::getToiletId,
                Facility::setToiletName,
                toiletMapper);
    }

    /**
     * 工单状态 → 设施状态 映射
     * PENDING → FAULT（已报修未处理）
     * REPAIRING → REPAIR（维修中）
     * CHECKING → REPAIR（待验收期间仍视为维修中）
     * 无有效工单 → NORMAL（正常）
     */
    private static final Map<String, String> ORDER_TO_FACILITY_STATUS = new HashMap<>();
    static {
        ORDER_TO_FACILITY_STATUS.put("PENDING", "FAULT");
        ORDER_TO_FACILITY_STATUS.put("REPAIRING", "REPAIR");
        ORDER_TO_FACILITY_STATUS.put("CHECKING", "REPAIR");
    }

    @Override
    @Transactional
    public void recomputeFacilityStatus(Long facilityId) {
        if (facilityId == null) return;
        int rows = facilityMapper.recomputeFacilityStatus(facilityId);
        if (rows > 0) {
            log.info("设施状态已按工单重建, id={}", facilityId);
        } else {
            log.warn("设施不存在, id={}", facilityId);
        }
    }

    @Override
    @Transactional
    public void syncFacilityStatus(Long facilityId, String targetStatus) {
        int retries = 0;
        while (retries < MAX_RETRIES) {
            Integer version = facilityMapper.selectVersionById(facilityId);
            if (version == null) {
                log.warn("设施不存在, id={}", facilityId);
                return;
            }
            int rows = facilityMapper.updateStatusWithVersion(facilityId, targetStatus, version);
            if (rows > 0) {
                log.info("设施状态同步成功, id={}, status={}, version={}", facilityId, targetStatus, version);
                return;
            }
            retries++;
            log.warn("设施状态乐观锁冲突, id={}, status={}, retry={}/{}", facilityId, targetStatus, retries, MAX_RETRIES);
            if (retries >= MAX_RETRIES) {
                throw BusinessException.badRequest("设施状态更新失败，并发冲突，请重试");
            }
            try {
                Thread.sleep(50L * retries);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw BusinessException.badRequest("设施状态更新被中断");
            }
        }
    }

    @Override
    @Transactional
    public int rebuildAllStatus() {
        // 查出所有非逻辑删除的设施
        List<Facility> all = facilityMapper.selectList(null);
        if (all.isEmpty()) return 0;

        // 批量查出所有设施最新的非终态工单（PENDING/REPAIRING/CHECKING）
        List<Long> facilityIds = all.stream().map(Facility::getId).collect(Collectors.toList());
        List<RepairOrder> activeOrders = repairOrderMapper.selectList(
                new LambdaQueryWrapper<RepairOrder>()
                        .in(RepairOrder::getFacilityId, facilityIds)
                        .in(RepairOrder::getStatus, "PENDING", "REPAIRING", "CHECKING")
                        .orderByDesc(RepairOrder::getCreateTime));

        // 设施ID → 最新工单状态
        Map<Long, String> latestOrderStatus = new HashMap<>();
        for (RepairOrder order : activeOrders) {
            latestOrderStatus.putIfAbsent(order.getFacilityId(), order.getStatus());
        }

        int fixed = 0;
        for (Facility facility : all) {
            String expectedStatus;
            String orderStatus = latestOrderStatus.get(facility.getId());
            if (orderStatus != null) {
                expectedStatus = ORDER_TO_FACILITY_STATUS.get(orderStatus);
            } else {
                expectedStatus = "NORMAL";
            }

            if (expectedStatus == null) {
                log.warn("未知工单状态映射, facilityId={}, orderStatus={}", facility.getId(), orderStatus);
                continue;
            }

            if (expectedStatus.equals(facility.getStatus())) {
                continue; // 已一致，跳过
            }

            // 尝试修正（乐观锁）
            Integer version = facilityMapper.selectVersionById(facility.getId());
            if (version == null) continue;
            int rows = facilityMapper.updateStatusWithVersion(facility.getId(), expectedStatus, version);
            if (rows > 0) {
                fixed++;
                log.info("设施状态已修正, id={}, {}→{}, version={}",
                        facility.getId(), facility.getStatus(), expectedStatus, version);
            } else {
                log.warn("设施状态修正乐观锁冲突, id={}, version={}", facility.getId(), version);
            }
        }

        log.info("设施状态重建完成, 共检查{}个设施, 修正{}个", all.size(), fixed);
        return fixed;
    }
}
