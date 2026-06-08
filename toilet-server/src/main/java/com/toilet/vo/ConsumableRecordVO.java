package com.toilet.vo;

import com.toilet.entity.ConsumableRecord;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
public class ConsumableRecordVO {
    private Long id;
    private Long toiletId;
    private String consumableName;
    private Integer quantity;
    private String type;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime createTime;
    private String toiletName;

    public static ConsumableRecordVO fromEntity(ConsumableRecord record) {
        if (record == null) return null;
        ConsumableRecordVO vo = new ConsumableRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }
}
