package com.toilet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.toilet.entity.IncidentReport;

import java.util.List;

public interface IncidentReportService extends IService<IncidentReport> {
    void populateDisplay(List<IncidentReport> list);
}
