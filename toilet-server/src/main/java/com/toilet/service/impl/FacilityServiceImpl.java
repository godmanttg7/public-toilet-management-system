package com.toilet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.toilet.common.DisplayUtils;
import com.toilet.entity.Facility;
import com.toilet.exception.BusinessException;
import com.toilet.mapper.FacilityMapper;
import com.toilet.mapper.ToiletMapper;
import com.toilet.service.FacilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FacilityServiceImpl extends ServiceImpl<FacilityMapper, Facility> implements FacilityService {

    private static final Logger log = LoggerFactory.getLogger(FacilityServiceImpl.class);
    private static final int MAX_RETRIES = 3;

    private final FacilityMapper facilityMapper;
    private final ToiletMapper toiletMapper;

    public FacilityServiceImpl(FacilityMapper facilityMapper, ToiletMapper toiletMapper) {
        super();
        this.facilityMapper = facilityMapper;
        this.toiletMapper = toiletMapper;
    }

    @Override
    public void populateDisplay(List<Facility> list) {
        DisplayUtils.populateToiletName(list,
                Facility::getToiletId,
                Facility::setToiletName,
                toiletMapper);
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
            // 短暂等待避免活锁
            try {
                Thread.sleep(50L * retries);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw BusinessException.badRequest("设施状态更新被中断");
            }
        }
    }
}
