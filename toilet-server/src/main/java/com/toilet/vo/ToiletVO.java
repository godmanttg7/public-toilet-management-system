package com.toilet.vo;

import com.toilet.entity.Toilet;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ToiletVO {
    private Long id;
    private String name;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String district;
    private String openTime;
    private Integer maleCount;
    private Integer femaleCount;
    private Integer accessibleCount;
    private String manageUnit;
    private String phone;
    private Integer status;
    private LocalDateTime createTime;

    public static ToiletVO fromEntity(Toilet toilet) {
        if (toilet == null) return null;
        ToiletVO vo = new ToiletVO();
        BeanUtils.copyProperties(toilet, vo);
        return vo;
    }
}
