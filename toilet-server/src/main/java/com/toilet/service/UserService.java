package com.toilet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.toilet.entity.User;

public interface UserService extends IService<User> {
    void resetPassword(Long userId);
}
