package com.example.springboottest.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 密码加密工具（SHA-256 + 固定盐）
 *
 * 说明：这是教学用的简化方案，够用来防止数据库里出现明文密码。
 * 真实项目里建议换成 BCrypt（spring-security-crypto），自带随机盐、更安全。
 */
public class PasswordUtil {

    /** 固定盐值，改动后历史密码会全部失效 */
    private static final String SALT = "hello_kitty_salt";

    /**
     * 把明文密码加密成 64 位十六进制字符串
     */
    public static String encode(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((rawPassword + SALT).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(b & 0xff);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 是 JDK 必备算法，正常不会走到这里
            throw new IllegalStateException("密码加密失败", e);
        }
    }

    /**
     * 校验密码是否正确
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return encode(rawPassword).equals(encodedPassword);
    }
}
