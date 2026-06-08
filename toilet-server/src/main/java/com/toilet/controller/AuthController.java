package com.toilet.controller;

import com.toilet.common.Result;
import com.toilet.dto.LoginDTO;
import com.toilet.dto.LoginResponse;
import com.toilet.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    @GetMapping("/info")
    public Result<LoginResponse> info(Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        return Result.success(authService.getUserInfo(userId));
    }
}
