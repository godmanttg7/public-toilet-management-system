package com.toilet.service;

import com.toilet.entity.User;
import com.toilet.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务单元测试
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testResetPassword() {
        // 重置用户密码
        Long userId = 1L;
        assertDoesNotThrow(() -> userService.resetPassword(userId));

        // 验证密码是否已重置
        User user = userService.getById(userId);
        assertNotNull(user.getPassword());
        assertTrue(user.getPassword().startsWith("$2a$"));

        // 验证密码可以匹配（因为使用了默认密码123456）
        boolean matches = passwordEncoder.matches("123456", user.getPassword());
        assertTrue(matches);

        System.out.println("UserService - resetPassword 测试通过！");
    }

    @Test
    void testResetPasswordNonExistentUser() {
        // 尝试重置不存在的用户
        assertThrows(BusinessException.class, () -> {
            userService.resetPassword(999L);
        });

        System.out.println("UserService - 重置不存在用户测试通过！");
    }

    @Test
    void testUserPasswordEncryption() {
        // 测试密码加密
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpassword123");
        user.setRealName("测试用户");
        user.setRole("USER");
        user.setStatus(1);
        user.setCreateTime(java.time.LocalDateTime.now());

        // 手动加密密码
        String encodedPassword = passwordEncoder.encode("testpassword123");
        user.setPassword(encodedPassword);

        // 保存用户
        assertDoesNotThrow(() -> userService.save(user));

        // 验证密码已加密
        User savedUser = userService.getById(user.getId());
        assertNotNull(savedUser.getPassword());

        // 验证密码可以被正确验证
        boolean matches = passwordEncoder.matches("testpassword123", savedUser.getPassword());
        assertTrue(matches);

        System.out.println("UserService - 密码加密测试通过！");
    }

    @Test
    void testPasswordEncoder() {
        // 测试BCrypt密码编码
        String rawPassword = "mySecurePassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // 验证哈希长度
        assertEquals(60, encodedPassword.length());

        // 验证可以匹配
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));

        // 验证不能匹配错误的密码
        assertFalse(passwordEncoder.matches("wrongPassword", encodedPassword));

        System.out.println("UserService - PasswordEncoder 测试通过！");
    }
}
