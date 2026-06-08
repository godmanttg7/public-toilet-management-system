package com.toilet.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.toilet.common.Result;
import com.toilet.dto.ConsumableOperateDTO;
import com.toilet.entity.ConsumableRecord;
import com.toilet.entity.ConsumableStock;
import com.toilet.service.ConsumableService;
import com.toilet.vo.ConsumableRecordVO;
import com.toilet.vo.ConsumableStockVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/consumable")
public class ConsumableController {

    private final ConsumableService consumableService;

    public ConsumableController(ConsumableService consumableService) {
        this.consumableService = consumableService;
    }

    @GetMapping("/page")
    public Result<IPage<ConsumableStockVO>> page(@RequestParam(defaultValue = "1") Integer current,
                                                 @RequestParam(defaultValue = "10") Integer size,
                                                 @RequestParam(required = false) Long toiletId) {
        LambdaQueryWrapper<ConsumableStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(toiletId != null, ConsumableStock::getToiletId, toiletId);
        IPage<ConsumableStock> page = consumableService.page(new Page<>(current, size), wrapper);
        consumableService.populateDisplay(page.getRecords());
        return Result.success(page.convert(ConsumableStockVO::fromEntity));
    }

    @GetMapping("/names")
    public Result<List<String>> getConsumableNames() {
        List<String> names = consumableService.list()
                .stream().map(ConsumableStock::getConsumableName)
                .distinct().sorted().collect(Collectors.toList());
        return Result.success(names);
    }

    @GetMapping("/alerts")
    public Result<List<ConsumableStockVO>> alerts() {
        LambdaQueryWrapper<ConsumableStock> wrapper = new LambdaQueryWrapper<>();
        // apply 用于列与列之间的比较，LambdaQueryWrapper 不支持列值直接比较
        wrapper.apply("current_stock < min_stock");
        List<ConsumableStock> list = consumableService.list(wrapper);
        consumableService.populateDisplay(list);
        return Result.success(list.stream().map(ConsumableStockVO::fromEntity).collect(Collectors.toList()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/in")
    public Result<Void> stockIn(@Valid @RequestBody ConsumableOperateDTO dto, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        consumableService.stockIn(dto.getToiletId(), dto.getConsumableName(), dto.getQuantity(), userId);
        return Result.success();
    }

    @PreAuthorize("hasAnyRole('CLEANER', 'ADMIN')")
    @PostMapping("/out")
    public Result<Void> stockOut(@Valid @RequestBody ConsumableOperateDTO dto, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        consumableService.stockOut(dto.getToiletId(), dto.getConsumableName(), dto.getQuantity(), userId);
        return Result.success();
    }

    @GetMapping("/record/page")
    public Result<IPage<ConsumableRecordVO>> recordPage(@RequestParam(defaultValue = "1") Integer current,
                                                        @RequestParam(defaultValue = "10") Integer size,
                                                        @RequestParam(required = false) Long toiletId) {
        LambdaQueryWrapper<ConsumableRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(toiletId != null, ConsumableRecord::getToiletId, toiletId)
               .orderByDesc(ConsumableRecord::getCreateTime);
        IPage<ConsumableRecord> page = consumableService.pageRecord(new Page<>(current, size), wrapper);
        consumableService.populateRecordDisplay(page.getRecords());
        return Result.success(page.convert(ConsumableRecordVO::fromEntity));
    }
}
