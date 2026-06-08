package com.toilet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.toilet.entity.Toilet;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ToiletMapper extends BaseMapper<Toilet> {
}
