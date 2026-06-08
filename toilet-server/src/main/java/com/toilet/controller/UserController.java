package com.toilet.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.toilet.common.Result;
import com.toilet.entity.User;
import com.toilet.service.UserService;
import com.toilet.vo.UserVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/page")
    public Result<IPage<UserVO>> page(@RequestParam(defaultValue = "1") Integer current,
                                      @RequestParam(defaultValue = "10") Integer size,
                                      @RequestParam(required = false) String realName,
                                      @RequestParam(required = false) String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(realName), User::getRealName, realName)
               .eq(StringUtils.hasText(role), User::getRole, role)
               .orderByDesc(User::getCreateTime);
        IPage<User> page = userService.page(new Page<>(current, size), wrapper);
        return Result.success(page.convert(UserVO::fromEntity));
    }

    @GetMapping("/list")
    public Result<List<UserVO>> list(@RequestParam(required = false) String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(role), User::getRole, role)
               .eq(User::getStatus, 1);
        List<User> list = userService.list(wrapper);
        return Result.success(list.stream().map(UserVO::fromEntity).collect(Collectors.toList()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Result<Void> save(@Valid @RequestBody User user) {
        String rawPassword = user.getPassword();
        if (rawPassword == null || rawPassword.isBlank()) {
            rawPassword = "123456";
        }
        user.setPassword(passwordEncoder.encode(rawPassword));
        userService.save(user);
        return Result.success();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody User user) {
        user.setPassword(null);
        userService.updateById(user);
        return Result.success();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/reset-pwd/{id}")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success();
    }
}
