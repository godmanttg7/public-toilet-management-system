package com.toilet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.toilet.entity.User;
import com.toilet.exception.BusinessException;
import com.toilet.mapper.UserMapper;
import com.toilet.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void resetPassword(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.dataNotFound("用户");
        }
        user.setPassword(passwordEncoder.encode("123456"));
        userMapper.updateById(user);
    }
}
