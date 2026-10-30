package com.example.springboottest.security;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理员登录凭证（token）的存放处
 *
 * 说明：为了不引入 JWT / Spring Security 这些额外依赖，这里用「内存 Map」保存 token。
 *   - 优点：简单、够用，重启服务自动清空（等于所有人被踢下线）
 *   - 缺点：服务重启后 token 失效、不支持多实例部署
 * 对教学 / 演示项目来说完全够用，以后要上生产再换成 JWT 或 Redis 即可。
 */
@Component
public class AdminTokenStore {

    /** token 有效期：8 小时 */
    private static final long TTL_MILLIS = 8L * 60 * 60 * 1000;

    /** token -> 会话信息 */
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    /**
     * 登录成功后签发一个 token
     */
    public Session issue(Long adminId, String username, String nickname) {
        cleanExpired();
        String token = UUID.randomUUID().toString().replace("-", "");
        Session session = new Session(adminId, username, nickname, System.currentTimeMillis() + TTL_MILLIS);
        sessions.put(token, session);
        return new Session(token, session.adminId, session.username, session.nickname, session.expireAt);
    }

    /**
     * 根据 token 取会话，不存在或已过期返回 null
     */
    public Session get(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        Session session = sessions.get(token);
        if (session == null) {
            return null;
        }
        if (session.expireAt < System.currentTimeMillis()) {
            sessions.remove(token);
            return null;
        }
        return session;
    }

    /** 退出登录 */
    public void revoke(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    /** 顺手清理过期 token，避免 Map 越来越大 */
    private void cleanExpired() {
        long now = System.currentTimeMillis();
        sessions.entrySet().removeIf(e -> e.getValue().expireAt < now);
    }

    /**
     * 一次管理员会话
     * token 为 null 表示「这是 issue 内部用的一半数据」，对外返回时都会带上 token
     */
    public static class Session {

        private final String token;
        private final Long adminId;
        private final String username;
        private final String nickname;
        private final long expireAt;

        public Session(Long adminId, String username, String nickname, long expireAt) {
            this(null, adminId, username, nickname, expireAt);
        }

        public Session(String token, Long adminId, String username, String nickname, long expireAt) {
            this.token = token;
            this.adminId = adminId;
            this.username = username;
            this.nickname = nickname;
            this.expireAt = expireAt;
        }

        public String getToken() {
            return token;
        }

        public Long getAdminId() {
            return adminId;
        }

        public String getUsername() {
            return username;
        }

        public String getNickname() {
            return nickname;
        }

        public long getExpireAt() {
            return expireAt;
        }

        /** 剩余有效秒数 */
        public long getExpiresIn() {
            return Math.max(0, (expireAt - System.currentTimeMillis()) / 1000);
        }

        /** 过期时间（人看的） */
        public LocalDateTime getExpireTime() {
            return LocalDateTime.now().plusSeconds(getExpiresIn());
        }
    }
}
