package com.toilet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.toilet.entity.CleanSchedule;
import com.toilet.entity.ConsumableRecord;
import com.toilet.entity.ConsumableStock;
import com.toilet.entity.Message;
import com.toilet.entity.Toilet;
import com.toilet.entity.User;
import com.toilet.exception.BusinessException;
import com.toilet.mapper.CleanScheduleMapper;
import com.toilet.mapper.ConsumableRecordMapper;
import com.toilet.mapper.ConsumableStockMapper;
import com.toilet.mapper.MessageMapper;
import com.toilet.mapper.ToiletMapper;
import com.toilet.mapper.UserMapper;
import com.toilet.service.ConsumableService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConsumableServiceImpl extends ServiceImpl<ConsumableStockMapper, ConsumableStock>
        implements ConsumableService {

    private final ConsumableStockMapper stockMapper;
    private final ConsumableRecordMapper recordMapper;
    private final ToiletMapper toiletMapper;
    private final UserMapper userMapper;
    private final MessageMapper messageMapper;
    private final CleanScheduleMapper cleanScheduleMapper;

    public ConsumableServiceImpl(ConsumableStockMapper stockMapper, ConsumableRecordMapper recordMapper, ToiletMapper toiletMapper, UserMapper userMapper, MessageMapper messageMapper, CleanScheduleMapper cleanScheduleMapper) {
        this.stockMapper = stockMapper;
        this.recordMapper = recordMapper;
        this.toiletMapper = toiletMapper;
        this.userMapper = userMapper;
        this.messageMapper = messageMapper;
        this.cleanScheduleMapper = cleanScheduleMapper;
    }

    @Override
    @Transactional
    public void stockIn(Long toiletId, String consumableName, Integer quantity, Long operatorId) {
        if (quantity <= 0) {
            throw BusinessException.badRequest("入库数量必须大于0");
        }
        // 查找或创建库存记录
        ConsumableStock stock = stockMapper.selectOne(new LambdaQueryWrapper<ConsumableStock>()
                .eq(ConsumableStock::getToiletId, toiletId)
                .eq(ConsumableStock::getConsumableName, consumableName));
        if (stock == null) {
            stock = new ConsumableStock();
            stock.setToiletId(toiletId);
            stock.setConsumableName(consumableName);
            stock.setUnit("个");
            stock.setCurrentStock(quantity);
            stock.setMinStock(10);
            stock.setUpdateTime(LocalDateTime.now());
            stockMapper.insert(stock);
        } else {
            stock.setCurrentStock(stock.getCurrentStock() + quantity);
            stock.setUpdateTime(LocalDateTime.now());
            stockMapper.updateById(stock);
        }
        // 记录出入库流水
        ConsumableRecord record = new ConsumableRecord();
        record.setToiletId(toiletId);
        record.setConsumableName(consumableName);
        record.setQuantity(quantity);
        record.setType("IN");
        record.setOperatorId(operatorId);
        recordMapper.insert(record);
    }

    @Override
    @Transactional
    public void stockOut(Long toiletId, String consumableName, Integer quantity, Long operatorId) {
        if (quantity <= 0) {
            throw BusinessException.badRequest("出库数量必须大于0");
        }
        ConsumableStock stock = stockMapper.selectOne(new LambdaQueryWrapper<ConsumableStock>()
                .eq(ConsumableStock::getToiletId, toiletId)
                .eq(ConsumableStock::getConsumableName, consumableName));
        if (stock == null) {
            throw BusinessException.badRequest("该耗材库存不存在");
        }
        if (stock.getCurrentStock() < quantity) {
            throw BusinessException.badRequest("库存不足，当前库存: " + stock.getCurrentStock());
        }
        int newStock = stock.getCurrentStock() - quantity;
        stock.setCurrentStock(newStock);
        stock.setUpdateTime(LocalDateTime.now());
        stockMapper.updateById(stock);
        // 记录出入库流水
        ConsumableRecord record = new ConsumableRecord();
        record.setToiletId(toiletId);
        record.setConsumableName(consumableName);
        record.setQuantity(quantity);
        record.setType("OUT");
        record.setOperatorId(operatorId);
        recordMapper.insert(record);
        // 库存低于下限 → 发送预警消息
        if (newStock < stock.getMinStock()) {
            String toiletName = "";
            Toilet t = toiletMapper.selectById(toiletId);
            if (t != null) toiletName = t.getName();
            String title = "耗材预警提醒";
            String content = String.format("【%s】%s库存仅剩%d，低于下限%d，请及时补充。",
                    toiletName, consumableName, newStock, stock.getMinStock());
            // 通知管理员
            List<User> admins = userMapper.selectList(
                    new LambdaQueryWrapper<User>().eq(User::getRole, "ADMIN"));
            for (User admin : admins) {
                Message msg = new Message();
                msg.setUserId(admin.getId());
                msg.setTitle(title);
                msg.setContent(content);
                msg.setIsRead(0);
                messageMapper.insert(msg);
            }
            // 通知该公厕有排班的保洁员
            List<Long> cleanerIds = cleanScheduleMapper.selectList(
                    new LambdaQueryWrapper<CleanSchedule>()
                            .eq(CleanSchedule::getToiletId, toiletId)
                            .select(CleanSchedule::getCleanerId)
                            .groupBy(CleanSchedule::getCleanerId))
                    .stream().map(CleanSchedule::getCleanerId).distinct().collect(Collectors.toList());
            List<User> cleaners = cleanerIds.isEmpty() ? java.util.Collections.emptyList()
                    : userMapper.selectBatchIds(cleanerIds);
            for (User cleaner : cleaners) {
                Message msg = new Message();
                msg.setUserId(cleaner.getId());
                msg.setTitle(title);
                msg.setContent(content);
                msg.setIsRead(0);
                messageMapper.insert(msg);
            }
        }
    }

    @Override
    public IPage<ConsumableRecord> pageRecord(Page<ConsumableRecord> page, LambdaQueryWrapper<ConsumableRecord> wrapper) {
        return recordMapper.selectPage(page, wrapper);
    }

    @Override
    public void populateDisplay(List<ConsumableStock> list) {
        if (list == null || list.isEmpty()) return;
        List<Long> toiletIds = list.stream()
                .map(ConsumableStock::getToiletId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (toiletIds.isEmpty()) return;
        Map<Long, String> nameMap = toiletMapper.selectBatchIds(toiletIds).stream()
                .collect(Collectors.toMap(Toilet::getId, Toilet::getName));
        list.forEach(s -> {
            if (s.getToiletId() != null) s.setToiletName(nameMap.get(s.getToiletId()));
        });
    }

    @Override
    public void populateRecordDisplay(List<ConsumableRecord> list) {
        if (list == null || list.isEmpty()) return;
        // 填充公厕名称
        List<Long> toiletIds = list.stream()
                .map(ConsumableRecord::getToiletId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (!toiletIds.isEmpty()) {
            Map<Long, String> nameMap = toiletMapper.selectBatchIds(toiletIds).stream()
                    .collect(Collectors.toMap(Toilet::getId, Toilet::getName));
            list.forEach(r -> {
                if (r.getToiletId() != null) r.setToiletName(nameMap.get(r.getToiletId()));
            });
        }
        // 填充操作人姓名
        List<Long> operatorIds = list.stream()
                .map(ConsumableRecord::getOperatorId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (!operatorIds.isEmpty()) {
            Map<Long, String> userNameMap = userMapper.selectBatchIds(operatorIds).stream()
                    .collect(Collectors.toMap(com.toilet.entity.User::getId, com.toilet.entity.User::getRealName));
            list.forEach(r -> {
                if (r.getOperatorId() != null) r.setOperatorName(userNameMap.get(r.getOperatorId()));
            });
        }
    }
}
