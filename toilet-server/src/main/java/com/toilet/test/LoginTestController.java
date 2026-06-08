package com.toilet.test;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.toilet.entity.User;
import com.toilet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@Profile("dev")
@RequestMapping("/api/test")
public class LoginTestController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/verify-admin-password")
    public String verifyAdminPassword() {
        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, "admin"));

        if (user == null) {
            return "User not found";
        }

        String password = "admin123";
        String dbHash = user.getPassword();

        boolean matches = passwordEncoder.matches(password, dbHash);

        return "Username: " + user.getUsername() + "\n" +
               "Password: " + password + "\n" +
               "DB Hash: " + dbHash + "\n" +
               "Hash Type: " + dbHash.substring(0, 7) + "\n" +
               "Matches: " + matches + "\n" +
               "Expected Hash Prefix: $2a$10$N.zmdr9k7uOCQb376NoUnuT\n" +
               "Actual Hash Prefix: " + dbHash.substring(0, 30);
    }

    @GetMapping("/list-all-users")
    public String listAllUsers() {
        return userService.list().stream()
                .map(u -> u.getUsername() + ":" + u.getPassword().substring(0, 30))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("No users found");
    }
}
