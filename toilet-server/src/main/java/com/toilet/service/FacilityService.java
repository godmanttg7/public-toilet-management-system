package com.toilet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.toilet.entity.Facility;

public interface FacilityService extends IService<Facility> {
    void populateDisplay(java.util.List<Facility> list);
    void syncFacilityStatus(Long facilityId, String targetStatus);

    /**
     * 根据工单表重新计算单个设施状态，替代手动 sync。
     * 在每次工单变更（创建/指派/更新状态/取消/删除）后调用。
     * PENDING → FAULT | REPAIRING/CHECKING → REPAIR | 无工单 → NORMAL
     */
    void recomputeFacilityStatus(Long facilityId);

    /**
     * 重建所有设施状态：根据最新的工单记录重新计算并修正设施状态。
     * 解决因同步遗漏、并发冲突导致的状态不一致问题。
     * @return 修正的设施数量
     */
    int rebuildAllStatus();
}
