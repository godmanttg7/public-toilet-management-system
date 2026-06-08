package com.toilet.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.toilet.common.Result;
import com.toilet.dto.ScoreDTO;
import com.toilet.entity.CleanSchedule;
import com.toilet.service.CleanScheduleService;
import com.toilet.vo.CleanScheduleVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/schedule")
public class CleanScheduleController {

    private final CleanScheduleService cleanScheduleService;

    public CleanScheduleController(CleanScheduleService cleanScheduleService) {
        this.cleanScheduleService = cleanScheduleService;
    }

    @GetMapping("/page")
    public Result<IPage<CleanScheduleVO>> page(@RequestParam(defaultValue = "1") Integer current,
                                                @RequestParam(defaultValue = "10") Integer size,
                                                @RequestParam(required = false) Long toiletId,
                                                @RequestParam(required = false) Long cleanerId,
                                                @RequestParam(required = false) String status) {
        LambdaQueryWrapper<CleanSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(toiletId != null, CleanSchedule::getToiletId, toiletId)
               .eq(cleanerId != null, CleanSchedule::getCleanerId, cleanerId)
               .eq(status != null, CleanSchedule::getStatus, status)
               .orderByDesc(CleanSchedule::getScheduleDate);
        IPage<CleanSchedule> page = cleanScheduleService.page(new Page<>(current, size), wrapper);
        cleanScheduleService.populateDisplay(page.getRecords());
        return Result.success(page.convert(CleanScheduleVO::fromEntity));
    }

    @GetMapping("/my")
    public Result<IPage<CleanScheduleVO>> mySchedules(@RequestParam(defaultValue = "1") Integer current,
                                                       @RequestParam(defaultValue = "10") Integer size,
                                                       Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        LambdaQueryWrapper<CleanSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CleanSchedule::getCleanerId, userId)
               .ge(CleanSchedule::getScheduleDate, LocalDate.now().minusDays(7))
               .orderByDesc(CleanSchedule::getScheduleDate);
        IPage<CleanSchedule> page = cleanScheduleService.page(new Page<>(current, size), wrapper);
        cleanScheduleService.populateDisplay(page.getRecords());
        return Result.success(page.convert(CleanScheduleVO::fromEntity));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Result<Void> save(@Valid @RequestBody CleanSchedule schedule) {
        cleanScheduleService.saveSchedule(schedule);
        return Result.success();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cleanScheduleService.removeById(id);
        return Result.success();
    }

    @PreAuthorize("hasAnyRole('CLEANER', 'ADMIN')")
    @PutMapping("/checkin/{id}")
    public Result<Void> checkin(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        cleanScheduleService.checkin(id, userId);
        return Result.success();
    }

    @PreAuthorize("hasAnyRole('CLEANER', 'ADMIN')")
    @PutMapping("/signoff/{id}")
    public Result<Void> signoff(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        cleanScheduleService.signoff(id, userId);
        return Result.success();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/score")
    public Result<Void> score(@Valid @RequestBody ScoreDTO dto) {
        cleanScheduleService.score(dto.getScheduleId(), dto.getScore(), dto.getRemark());
        return Result.success();
    }
}
