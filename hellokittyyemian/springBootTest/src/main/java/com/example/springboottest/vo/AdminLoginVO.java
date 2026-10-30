package com.example.springboottest.vo;

/**
 * 后台登录成功后返回给前端的信息
 */
public class AdminLoginVO {

    /** 后续所有后台接口都要在请求头里带这个 token */
    private String token;
    private Long adminId;
    private String username;
    private String nickname;
    /** 有效期剩余秒数 */
    private Long expiresIn;

    public AdminLoginVO() {
    }

    public AdminLoginVO(String token, Long adminId, String username, String nickname, Long expiresIn) {
        this.token = token;
        this.adminId = adminId;
        this.username = username;
        this.nickname = nickname;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
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

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
