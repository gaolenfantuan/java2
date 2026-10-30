package com.example.springboottest.service;

import com.example.springboottest.dto.LoginRequest;
import com.example.springboottest.dto.RegisterRequest;
import com.example.springboottest.vo.LoginVO;

/**
 * 用户业务接口
 */
public interface UserService {

    /**
     * 注册新用户，返回新用户 id
     */
    Long register(RegisterRequest request);

    /**
     * 登录，成功返回用户信息 + token，失败抛 IllegalArgumentException
     */
    LoginVO login(LoginRequest request);

    /**
     * 用户名是否已被占用
     */
    boolean isUsernameTaken(String username);
}
