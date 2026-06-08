package com.toilet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.toilet.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
