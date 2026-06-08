package com.toilet.vo;

import com.toilet.entity.ConsumableStock;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
public class ConsumableStockVO {
    private Long id;
    private Long toiletId;
    private String consumableName;
    private String unit;
    private Integer currentStock;
    private Integer minStock;
    private LocalDateTime updateTime;
    private String toiletName;

    public static ConsumableStockVO fromEntity(ConsumableStock stock) {
        if (stock == null) return null;
        ConsumableStockVO vo = new ConsumableStockVO();
        BeanUtils.copyProperties(stock, vo);
        return vo;
    }
}
