package com.toilet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.toilet.entity.IncidentReport;
import com.toilet.entity.Toilet;
import com.toilet.entity.User;
import com.toilet.mapper.IncidentReportMapper;
import com.toilet.mapper.ToiletMapper;
import com.toilet.mapper.UserMapper;
import com.toilet.service.IncidentReportService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class IncidentReportServiceImpl extends ServiceImpl<IncidentReportMapper, IncidentReport>
        implements IncidentReportService {

    private final IncidentReportMapper incidentReportMapper;
    private final ToiletMapper toiletMapper;
    private final UserMapper userMapper;

    public IncidentReportServiceImpl(IncidentReportMapper incidentReportMapper, ToiletMapper toiletMapper, UserMapper userMapper) {
        this.incidentReportMapper = incidentReportMapper;
        this.toiletMapper = toiletMapper;
        this.userMapper = userMapper;
    }

    @Override
    public void populateDisplay(List<IncidentReport> list) {
        if (list == null || list.isEmpty()) return;
        List<Long> toiletIds = list.stream().map(IncidentReport::getToiletId).filter(id -> id != null).distinct().collect(Collectors.toList());
        Map<Long, String> toiletMap = toiletMapper.selectBatchIds(toiletIds).stream().collect(Collectors.toMap(Toilet::getId, Toilet::getName));
        List<Long> userIds = list.stream().map(IncidentReport::getReporterId).filter(id -> id != null).distinct().collect(Collectors.toList());
        Map<Long, String> userMap = userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, User::getRealName));
        list.forEach(r -> {
            if (r.getToiletId() != null) r.setToiletName(toiletMap.get(r.getToiletId()));
            if (r.getReporterId() != null) r.setReporterName(userMap.get(r.getReporterId()));
        });
    }
}
