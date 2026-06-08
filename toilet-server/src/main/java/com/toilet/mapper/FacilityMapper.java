package com.toilet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.toilet.entity.Facility;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface FacilityMapper extends BaseMapper<Facility> {

    @Select("SELECT version FROM tb_facility WHERE id = #{id}")
    Integer selectVersionById(@Param("id") Long id);

    @Update("UPDATE tb_facility SET status = #{status}, version = version + 1 WHERE id = #{id} AND version = #{version}")
    int updateStatusWithVersion(@Param("id") Long id, @Param("status") String status, @Param("version") Integer version);

    @Update("UPDATE tb_facility SET "
            + "status = CASE "
            + "  WHEN EXISTS (SELECT 1 FROM tb_repair_order r WHERE r.facility_id = #{facilityId} AND r.deleted = 0 AND r.status IN ('REPAIRING','CHECKING')) THEN 'REPAIR' "
            + "  WHEN EXISTS (SELECT 1 FROM tb_repair_order r WHERE r.facility_id = #{facilityId} AND r.deleted = 0 AND r.status = 'PENDING') THEN 'FAULT' "
            + "  ELSE 'NORMAL' "
            + "END, "
            + "version = version + 1, "
            + "update_time = SYSDATE "
            + "WHERE id = #{facilityId}")
    int recomputeFacilityStatus(@Param("facilityId") Long facilityId);
}
