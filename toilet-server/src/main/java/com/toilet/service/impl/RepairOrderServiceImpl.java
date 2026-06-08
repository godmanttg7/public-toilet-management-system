package com.toilet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.toilet.entity.Facility;
import com.toilet.entity.Message;
import com.toilet.entity.RepairOrder;
import com.toilet.entity.Toilet;
import com.toilet.entity.User;
import com.toilet.exception.BusinessException;
import com.toilet.mapper.MessageMapper;
import com.toilet.mapper.RepairOrderMapper;
import com.toilet.mapper.ToiletMapper;
import com.toilet.mapper.UserMapper;
import com.toilet.service.FacilityService;
import com.toilet.service.RepairOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RepairOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrder>
        implements RepairOrderService {

    private final RepairOrderMapper repairOrderMapper;
    private final FacilityService facilityService;
    private final ToiletMapper toiletMapper;
    private final UserMapper userMapper;
    private final MessageMapper messageMapper;

    public RepairOrderServiceImpl(RepairOrderMapper repairOrderMapper, FacilityService facilityService,
                                  ToiletMapper toiletMapper, UserMapper userMapper, MessageMapper messageMapper) {
        this.repairOrderMapper = repairOrderMapper;
        this.facilityService = facilityService;
        this.toiletMapper = toiletMapper;
        this.userMapper = userMapper;
        this.messageMapper = messageMapper;
    }

    private void sendMessage(Long userId, String title, String content) {
        Message msg = new Message();
        msg.setUserId(userId);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setIsRead(0);
        messageMapper.insert(msg);
    }

    @Override
    @Transactional
    public void assignOrder(Long orderId, Long assigneeId) {
        RepairOrder order = repairOrderMapper.selectById(orderId);
        if (order == null) {
            throw BusinessException.badRequest("工单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw BusinessException.badRequest("仅待处理状态的工单可以指派");
        }
        // 校验维修人员是否存在且角色正确
        User repairer = userMapper.selectById(assigneeId);
        if (repairer == null) {
            throw BusinessException.badRequest("维修人员不存在");
        }
        if (!"REPAIR".equals(repairer.getRole())) {
            throw BusinessException.badRequest("所选用户不是维修人员");
        }
        order.setAssigneeId(assigneeId);
        order.setStatus("REPAIRING");
        repairOrderMapper.updateById(order);
        // 同步设施状态为维修中（乐观锁重试）
        if (order.getFacilityId() != null) {
            facilityService.syncFacilityStatus(order.getFacilityId(), "REPAIR");
        }
        // 通知被指派的维修人员
        sendMessage(assigneeId, "新工单指派",
                "您已被指派维修工单【" + order.getOrderNo() + "】，请及时处理。故障描述：" + order.getFaultDesc());
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        RepairOrder order = repairOrderMapper.selectById(orderId);
        if (order == null) {
            throw BusinessException.badRequest("工单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw BusinessException.badRequest("仅待处理状态的工单可以取消");
        }
        order.setStatus("CANCELLED");
        repairOrderMapper.updateById(order);
        // 同步设施状态为正常（乐观锁重试）
        if (order.getFacilityId() != null) {
            facilityService.syncFacilityStatus(order.getFacilityId(), "NORMAL");
        }
        // 通知上报人工单已取消
        if (order.getReporterId() != null) {
            sendMessage(order.getReporterId(), "工单已取消",
                    "您上报的工单【" + order.getOrderNo() + "】已被管理员取消。");
        }
    }

    @Override
    @Transactional
    public void updateStatus(Long orderId, String newStatus, String repairDesc, String images, Long userId, String role) {
        RepairOrder order = repairOrderMapper.selectById(orderId);
        if (order == null) {
            throw BusinessException.badRequest("工单不存在");
        }
        String currentStatus = order.getStatus();

        // 验证状态流转合法性
        Set<String> allowed;
        switch (currentStatus) {
            case "PENDING":
                allowed = new HashSet<>(Arrays.asList("REPAIRING", "CANCELLED"));
                break;
            case "REPAIRING":
                allowed = new HashSet<>(Arrays.asList("CHECKING"));
                break;
            case "CHECKING":
                allowed = new HashSet<>(Arrays.asList("FINISHED", "REPAIRING"));
                break;
            case "FINISHED":
            case "CANCELLED":
                throw BusinessException.badRequest("已结束工单不可变更状态");
            default:
                throw BusinessException.badRequest("未知工单状态: " + currentStatus);
        }

        if (!allowed.contains(newStatus)) {
            throw BusinessException.badRequest(
                    "不允许从 " + currentStatus + " 变更为 " + newStatus);
        }

        // 权限校验
        if ("REPAIRING".equals(currentStatus) && "CHECKING".equals(newStatus)) {
            if (!"REPAIR".equals(role)) {
                throw BusinessException.badRequest("仅维修人员可完成维修");
            }
            if (!order.getAssigneeId().equals(userId)) {
                throw BusinessException.badRequest("只能操作自己被指派的工单");
            }
            order.setRepairDesc(repairDesc);
            if (images != null) {
                order.setImages(images);
            }
        } else if ("CHECKING".equals(currentStatus)) {
            if (!"ADMIN".equals(role)) {
                throw BusinessException.badRequest("仅管理员可验收工单");
            }
        }

        order.setStatus(newStatus);
        if ("FINISHED".equals(newStatus)) {
            order.setFinishTime(LocalDateTime.now());
            if (order.getFacilityId() != null) {
                facilityService.syncFacilityStatus(order.getFacilityId(), "NORMAL");
            }
            // 通知维修人员工单已验收通过
            if (order.getAssigneeId() != null) {
                sendMessage(order.getAssigneeId(), "工单验收通过",
                        "您维修的工单【" + order.getOrderNo() + "】已验收通过。");
            }
        } else if ("REPAIRING".equals(newStatus) && "CHECKING".equals(currentStatus)) {
            // 验收退回：设施恢复故障状态
            if (order.getFacilityId() != null) {
                facilityService.syncFacilityStatus(order.getFacilityId(), "FAULT");
            }
            // 通知维修人员工单被退回
            if (order.getAssigneeId() != null) {
                sendMessage(order.getAssigneeId(), "工单验收退回",
                        "您维修的工单【" + order.getOrderNo() + "】验收未通过，请重新处理。");
            }
        } else if ("REPAIRING".equals(newStatus) && "PENDING".equals(currentStatus)) {
            // 直接从待处理开始维修：设施进入维修中
            if (order.getFacilityId() != null) {
                facilityService.syncFacilityStatus(order.getFacilityId(), "REPAIR");
            }
        } else if ("CANCELLED".equals(newStatus) && "PENDING".equals(currentStatus)) {
            // 直接从待处理取消：设施恢复正常
            if (order.getFacilityId() != null) {
                facilityService.syncFacilityStatus(order.getFacilityId(), "NORMAL");
            }
            // 通知上报人工单已取消
            if (order.getReporterId() != null) {
                sendMessage(order.getReporterId(), "工单已取消",
                        "您上报的工单【" + order.getOrderNo() + "】已被管理员取消。");
            }
        }
        // REPAIRING → CHECKING 通知管理员有待验收工单
        if ("CHECKING".equals(newStatus) && "REPAIRING".equals(currentStatus)) {
            List<User> admins = userMapper.selectList(
                    new LambdaQueryWrapper<User>().eq(User::getRole, "ADMIN"));
            for (User admin : admins) {
                sendMessage(admin.getId(), "工单待验收",
                        "工单【" + order.getOrderNo() + "】已完成维修，请及时验收。");
            }
        }
        repairOrderMapper.updateById(order);
    }

    @Override
    public void populateDisplay(List<RepairOrder> list) {
        if (list == null || list.isEmpty()) return;
        // 批量查询公厕
        List<Long> toiletIds = list.stream().map(RepairOrder::getToiletId).filter(id -> id != null).distinct().collect(Collectors.toList());
        Map<Long, String> toiletMap = toiletMapper.selectBatchIds(toiletIds).stream().collect(Collectors.toMap(Toilet::getId, Toilet::getName));
        // 批量查询设施
        List<Long> facilityIds = list.stream().map(RepairOrder::getFacilityId).filter(id -> id != null).distinct().collect(Collectors.toList());
        Map<Long, String> facilityMap = facilityService.listByIds(facilityIds).stream().collect(Collectors.toMap(Facility::getId, Facility::getFacilityType));
        // 批量查询用户
        List<Long> userIds = list.stream().flatMap(o -> { Set<Long> ids = new HashSet<>(); if (o.getReporterId() != null) ids.add(o.getReporterId()); if (o.getAssigneeId() != null) ids.add(o.getAssigneeId()); return ids.stream(); }).distinct().collect(Collectors.toList());
        Map<Long, String> userMap = userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, User::getRealName));
        // 填充
        list.forEach(o -> {
            if (o.getToiletId() != null) o.setToiletName(toiletMap.get(o.getToiletId()));
            if (o.getFacilityId() != null) o.setFacilityType(facilityMap.get(o.getFacilityId()));
            if (o.getReporterName() == null && o.getReporterId() != null) o.setReporterName(userMap.get(o.getReporterId()));
            if (o.getAssigneeId() != null) o.setAssigneeName(userMap.get(o.getAssigneeId()));
        });
    }
}
