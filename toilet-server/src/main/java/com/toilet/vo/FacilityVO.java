package com.toilet.vo;

import com.toilet.entity.Facility;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FacilityVO {
    private Long id;
    private Long toiletId;
    private String facilityType;
    private String brand;
    private String model;
    private LocalDate installDate;
    private LocalDate warrantyDate;
    private String status;
    private LocalDateTime createTime;
    private String toiletName;

    public static FacilityVO fromEntity(Facility facility) {
        if (facility == null) return null;
        FacilityVO vo = new FacilityVO();
        BeanUtils.copyProperties(facility, vo);
        return vo;
    }
}
