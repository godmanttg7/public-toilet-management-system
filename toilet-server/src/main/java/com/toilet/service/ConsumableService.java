package com.toilet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.toilet.entity.ConsumableRecord;
import com.toilet.entity.ConsumableStock;

public interface ConsumableService extends IService<ConsumableStock> {
    void stockIn(Long toiletId, String consumableName, Integer quantity, Long operatorId);
    void stockOut(Long toiletId, String consumableName, Integer quantity, Long operatorId);
    IPage<ConsumableRecord> pageRecord(Page<ConsumableRecord> page, LambdaQueryWrapper<ConsumableRecord> wrapper);
    void populateDisplay(java.util.List<ConsumableStock> list);
    void populateRecordDisplay(java.util.List<ConsumableRecord> list);
}
