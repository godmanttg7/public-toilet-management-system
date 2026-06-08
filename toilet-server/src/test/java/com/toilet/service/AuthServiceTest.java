package com.toilet.service;

import com.toilet.dto.LoginDTO;
import com.toilet.dto.LoginResponse;
import com.toilet.entity.User;
import com.toilet.exception.BusinessException;
import com.toilet.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // 在事务中动态设置用户密码，确保 BCrypt hash 与运行环境一致
        User admin = userService.getById(1L);
        admin.setPassword(passwordEncoder.encode("123456"));
        userService.updateById(admin);

        User disabled = userService.getById(4L);
        disabled.setPassword(passwordEncoder.encode("123456"));
        userService.updateById(disabled);
    }

    @Test
    void testLoginSuccess() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");

        LoginResponse resp = authService.login(dto);
        assertNotNull(resp);
        assertNotNull(resp.getToken());
        assertEquals(1L, resp.getUserId());
        assertEquals("admin", resp.getUsername());
        assertEquals("系统管理员", resp.getRealName());
        assertEquals("ADMIN", resp.getRole());

        System.out.println("AuthService - 登录成功测试通过！");
    }

    @Test
    void testLoginWrongPassword() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("wrongpassword");

        assertThrows(BusinessException.class, () -> authService.login(dto));

        System.out.println("AuthService - 密码错误测试通过！");
    }

    @Test
    void testLoginNonExistentUser() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("nonexistent");
        dto.setPassword("123456");

        assertThrows(BusinessException.class, () -> authService.login(dto));

        System.out.println("AuthService - 用户不存在测试通过！");
    }

    @Test
    void testLoginDisabledAccount() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("disabled");
        dto.setPassword("123456");

        assertThrows(BusinessException.class, () -> authService.login(dto));

        System.out.println("AuthService - 禁用账号测试通过！");
    }

    @Test
    void testGetUserInfoSuccess() {
        LoginResponse resp = authService.getUserInfo(1L);
        assertNotNull(resp);
        assertEquals(1L, resp.getUserId());
        assertEquals("admin", resp.getUsername());
        assertEquals("ADMIN", resp.getRole());

        System.out.println("AuthService - 获取用户信息测试通过！");
    }

    @Test
    void testGetUserInfoNonExistent() {
        assertThrows(BusinessException.class, () -> authService.getUserInfo(999L));

        System.out.println("AuthService - 获取不存在用户信息测试通过！");
    }
}
