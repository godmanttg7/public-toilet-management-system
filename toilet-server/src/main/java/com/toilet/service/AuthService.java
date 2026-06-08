package com.toilet.service;

import com.toilet.dto.LoginDTO;
import com.toilet.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginDTO loginDTO);
    LoginResponse getUserInfo(Long userId);
}
