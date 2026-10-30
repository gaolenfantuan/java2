package com.example.springboottest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 注册请求参数（前端提交的 JSON 会自动转成这个对象）
 */
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度需要在 2-20 个字符之间")
    private String username;

    /** 昵称选填，不填就用用户名 */
    @Size(max = 20, message = "昵称最多 20 个字符")
    private String nickname;

    @NotBlank(message = "密码不能为空")
    @Size(min = 4, max = 32, message = "密码长度需要在 4-32 位之间")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
