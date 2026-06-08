package com.toilet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.toilet.entity.Toilet;
import com.toilet.mapper.ToiletMapper;
import com.toilet.service.ToiletService;
import org.springframework.stereotype.Service;

@Service
public class ToiletServiceImpl extends ServiceImpl<ToiletMapper, Toilet> implements ToiletService {
}
