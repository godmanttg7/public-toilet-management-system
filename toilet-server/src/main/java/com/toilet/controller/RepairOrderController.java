package com.toilet.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.toilet.common.Result;
import com.toilet.dto.AssignDTO;
import com.toilet.dto.ChangeStatusDTO;
import com.toilet.entity.RepairOrder;
import com.toilet.entity.User;
import com.toilet.mapper.UserMapper;
import com.toilet.service.FacilityService;
import com.toilet.service.RepairOrderService;
import com.toilet.vo.RepairOrderVO;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/repair")
public class RepairOrderController {

    private final RepairOrderService repairOrderService;
    private final FacilityService facilityService;
    private final UserMapper userMapper;

    public RepairOrderController(RepairOrderService repairOrderService, FacilityService facilityService,
                                  UserMapper userMapper) {
        this.repairOrderService = repairOrderService;
        this.facilityService = facilityService;
        this.userMapper = userMapper;
    }

    @GetMapping("/page")
    public Result<IPage<RepairOrderVO>> page(@RequestParam(defaultValue = "1") Integer current,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestParam(required = false) String status,
                                             @RequestParam(required = false) Long toiletId) {
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(status), RepairOrder::getStatus, status)
               .eq(toiletId != null, RepairOrder::getToiletId, toiletId)
               .orderByDesc(RepairOrder::getCreateTime);
        IPage<RepairOrder> page = repairOrderService.page(new Page<>(current, size), wrapper);
        repairOrderService.populateDisplay(page.getRecords());
        return Result.success(page.convert(RepairOrderVO::fromEntity));
    }

    @GetMapping("/my")
    public Result<IPage<RepairOrderVO>> myOrders(@RequestParam(defaultValue = "1") Integer current,
                                                  @RequestParam(defaultValue = "10") Integer size,
                                                  Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairOrder::getAssigneeId, userId)
               .orderByDesc(RepairOrder::getCreateTime);
        IPage<RepairOrder> page = repairOrderService.page(new Page<>(current, size), wrapper);
        repairOrderService.populateDisplay(page.getRecords());
        return Result.success(page.convert(RepairOrderVO::fromEntity));
    }

    @PostMapping
    @Transactional
    public Result<Void> report(@Valid @RequestBody RepairOrder order, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        order.setReporterId(userId);
        // 自动填充上报人姓名
        User currentUser = userMapper.selectById(userId);
        if (currentUser != null) {
            order.setReporterName(currentUser.getRealName());
        }
        order.setOrderNo("RO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        order.setStatus("PENDING");
        repairOrderService.save(order);
        // 同步更新设施状态（从工单表重新计算）
        if (order.getFacilityId() != null) {
            facilityService.recomputeFacilityStatus(order.getFacilityId());
        }
        return Result.success();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/assign")
    public Result<Void> assign(@Valid @RequestBody AssignDTO dto) {
        repairOrderService.assignOrder(dto.getOrderId(), dto.getAssigneeId());
        return Result.success();
    }

    @PutMapping("/status")
    public Result<Void> updateStatus(@Valid @RequestBody ChangeStatusDTO dto, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        String role = authentication.getAuthorities().stream()
                .map(g -> g.getAuthority().replace("ROLE_", ""))
                .filter(r -> r.equals("ADMIN") || r.equals("REPAIR"))
                .findFirst().orElse("");
        repairOrderService.updateStatus(dto.getOrderId(), dto.getNewStatus(), dto.getRepairDesc(), dto.getImages(), userId, role);
        return Result.success();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        repairOrderService.cancelOrder(id);
        return Result.success();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Transactional
    public Result<Void> delete(@PathVariable Long id) {
        RepairOrder order = repairOrderService.getById(id);
        if (order == null) return Result.success();
        Long facilityId = order.getFacilityId();
        // 先删除工单（逻辑删除标记 deleted=1）
        repairOrderService.removeById(id);
        // 再根据剩余工单重建设施状态
        if (facilityId != null) {
            facilityService.recomputeFacilityStatus(facilityId);
        }
        return Result.success();
    }
}
