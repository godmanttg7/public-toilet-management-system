package com.toilet.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.toilet.common.Result;
import com.toilet.entity.IncidentReport;
import com.toilet.service.IncidentReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/incident")
public class IncidentReportController {

    private final IncidentReportService incidentReportService;

    public IncidentReportController(IncidentReportService incidentReportService) {
        this.incidentReportService = incidentReportService;
    }

    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<IncidentReport>> page(@RequestParam(defaultValue = "1") Integer current,
                                              @RequestParam(defaultValue = "10") Integer size) {
        LambdaQueryWrapper<IncidentReport> wrapper = new LambdaQueryWrapper<IncidentReport>()
                .orderByDesc(IncidentReport::getCreateTime);
        IPage<IncidentReport> page = incidentReportService.page(new Page<>(current, size), wrapper);
        incidentReportService.populateDisplay(page.getRecords());
        return Result.success(page);
    }

    @GetMapping("/unresolved-count")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> getUnresolvedCount() {
        long count = incidentReportService.count(
                new LambdaQueryWrapper<IncidentReport>().eq(IncidentReport::getStatus, "PENDING"));
        return Result.success(count);
    }

    @GetMapping("/my")
    public Result<IPage<IncidentReport>> myReports(@RequestParam(defaultValue = "1") Integer current,
                                                    @RequestParam(defaultValue = "10") Integer size,
                                                    Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        LambdaQueryWrapper<IncidentReport> wrapper = new LambdaQueryWrapper<IncidentReport>()
                .eq(IncidentReport::getReporterId, userId)
                .orderByDesc(IncidentReport::getCreateTime);
        IPage<IncidentReport> page = incidentReportService.page(new Page<>(current, size), wrapper);
        incidentReportService.populateDisplay(page.getRecords());
        return Result.success(page);
    }

    @PostMapping
    public Result<Void> report(@RequestBody IncidentReport report, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        String role = authentication.getAuthorities().stream()
                .map(g -> g.getAuthority().replace("ROLE_", ""))
                .filter(r -> r.equals("CLEANER") || r.equals("REPAIR") || r.equals("ADMIN"))
                .findFirst().orElse("CLEANER");
        report.setReporterId(userId);
        report.setReporterRole(role);
        report.setStatus("PENDING");
        incidentReportService.save(report);
        return Result.success();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/resolve/{id}")
    public Result<Void> resolve(@PathVariable Long id) {
        IncidentReport report = incidentReportService.getById(id);
        if (report != null) {
            report.setStatus("RESOLVED");
            incidentReportService.updateById(report);
        }
        return Result.success();
    }
}
