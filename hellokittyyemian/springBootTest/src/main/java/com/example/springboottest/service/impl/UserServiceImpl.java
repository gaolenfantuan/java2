package com.example.springboottest.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.springboottest.dto.LoginRequest;
import com.example.springboottest.dto.RegisterRequest;
import com.example.springboottest.entity.User;
import com.example.springboottest.mapper.UserMapper;
import com.example.springboottest.service.UserService;
import com.example.springboottest.util.PasswordUtil;
import com.example.springboottest.vo.LoginVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 用户业务实现
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    /** 构造器注入（比 @Autowired 字段注入更推荐） */
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterRequest request) {
        String username = request.getUsername().trim();

        // 1. 用户名不能重复
        if (isUsernameTaken(username)) {
            throw new IllegalArgumentException("这个用户名已经被小猫咪占用啦，换一个吧~");
        }

        // 2. 组装实体
        User user = new User();
        user.setUsername(username);
        String nickname = request.getNickname();
        user.setNickname(nickname == null || nickname.isBlank() ? username : nickname.trim());
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setStatus(1);

        // 3. 插入数据库（createTime / updateTime 由 MyBatis-Plus 自动填充）
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    public LoginVO login(LoginRequest request) {
        String username = request.getUsername().trim();

        // 1. 按用户名查用户（逻辑删除的记录会被自动排除）
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getUsername, username)
        );

        // 2. 用户不存在或密码不对，统一提示，避免泄露"该用户名是否存在"
        if (user == null || !PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码不对哦，再检查一下~");
        }

        // 3. 账号被禁用
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new IllegalArgumentException("这个账号已被禁用啦，请联系管理员~");
        }

        // 4. 生成简单 token（演示用）
        String token = UUID.randomUUID().toString().replace("-", "");
        return new LoginVO(user.getId(), user.getUsername(), user.getNickname(), token);
    }

    @Override
    public boolean isUsernameTaken(String username) {
        Long count = userMapper.selectCount(
                Wrappers.<User>lambdaQuery().eq(User::getUsername, username)
        );
        return count != null && count > 0;
    }
}
