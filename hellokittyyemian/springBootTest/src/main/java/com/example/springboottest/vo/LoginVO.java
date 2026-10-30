package com.example.springboottest.vo;

/**
 * 登录成功后返回给前端的数据（不包含密码）
 */
public class LoginVO {

    private Long id;
    private String username;
    private String nickname;
    /** 简单令牌，前端可以存起来，后续请求带上（演示用，不是标准 JWT） */
    private String token;

    public LoginVO() {
    }

    public LoginVO(Long id, String username, String nickname, String token) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
