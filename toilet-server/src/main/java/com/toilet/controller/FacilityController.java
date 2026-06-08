package com.toilet.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.toilet.common.Result;
import com.toilet.entity.Facility;
import com.toilet.service.FacilityService;
import com.toilet.vo.FacilityVO;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/facility")
public class FacilityController {

    private final FacilityService facilityService;

    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    @GetMapping("/list")
    public Result<List<FacilityVO>> list(@RequestParam(required = false) Long toiletId) {
        LambdaQueryWrapper<Facility> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(toiletId != null, Facility::getToiletId, toiletId)
               .orderByDesc(Facility::getCreateTime);
        List<Facility> list = facilityService.list(wrapper);
        facilityService.populateDisplay(list);
        return Result.success(list.stream().map(FacilityVO::fromEntity).collect(Collectors.toList()));
    }

    @GetMapping("/page")
    public Result<IPage<FacilityVO>> page(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          @RequestParam(required = false) Long toiletId,
                                          @RequestParam(required = false) String facilityType,
                                          @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Facility> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(toiletId != null, Facility::getToiletId, toiletId)
               .eq(StringUtils.hasText(facilityType), Facility::getFacilityType, facilityType)
               .eq(StringUtils.hasText(status), Facility::getStatus, status)
               .orderByDesc(Facility::getCreateTime);
        IPage<Facility> page = facilityService.page(new Page<>(current, size), wrapper);
        facilityService.populateDisplay(page.getRecords());
        return Result.success(page.convert(FacilityVO::fromEntity));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Result<Void> save(@Valid @RequestBody Facility facility) {
        facilityService.save(facility);
        return Result.success();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody Facility facility) {
        facilityService.updateById(facility);
        return Result.success();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        facilityService.removeById(id);
        return Result.success();
    }
}
