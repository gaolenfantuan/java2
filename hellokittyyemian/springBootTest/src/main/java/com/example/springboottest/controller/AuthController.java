package com.example.springboottest.controller;

import com.example.springboottest.common.Result;
import com.example.springboottest.dto.LoginRequest;
import com.example.springboottest.dto.RegisterRequest;
import com.example.springboottest.service.UserService;
import com.example.springboottest.vo.LoginVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录 / 注册接口
 *
 * 前端（Vite 已配置 /api 代理到 localhost:8080）调用的路径：
 *   POST http://localhost:5173/api/auth/register
 *   POST http://localhost:5173/api/auth/login
 *   GET  http://localhost:5173/api/auth/check?username=xxx
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 注册
     * 请求体示例：{ "username": "kitty", "nickname": "小猫咪", "password": "1234" }
     */
    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = userService.register(request);
        return Result.success("注册成功，快去登录吧~", userId);
    }

    /**
     * 登录
     * 请求体示例：{ "username": "kitty", "password": "1234" }
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        LoginVO vo = userService.login(request);
        return Result.success("登录成功", vo);
    }

    /**
     * 检查用户名是否已被占用（前端输入框失焦时可以调用来实时提示）
     */
    @GetMapping("/check")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        // true 表示已被占用
        return Result.success(userService.isUsernameTaken(username));
    }
}
