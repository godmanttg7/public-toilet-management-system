package com.toilet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.toilet.entity.Facility;

public interface FacilityService extends IService<Facility> {
    void populateDisplay(java.util.List<Facility> list);
    void syncFacilityStatus(Long facilityId, String targetStatus);
}
