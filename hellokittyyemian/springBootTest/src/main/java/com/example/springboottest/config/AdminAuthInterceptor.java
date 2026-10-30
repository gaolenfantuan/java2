package com.example.springboottest.config;

import com.example.springboottest.security.AdminTokenStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 后台接口的登录校验
 *
 * 规则：访问 /api/admin/** 时，必须带上请求头 X-Admin-Token（登录接口除外）。
 * 没带 / 过期 -> 直接返回 { code: 401 }，前端收到后跳回后台登录页。
 *
 * 注意：HTTP 状态码依然是 200，业务状态放在 body 的 code 里，
 *      这样前端只用一个 request 方法就能统一处理。
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    /** 前端存放 token 的请求头名字 */
    public static final String TOKEN_HEADER = "X-Admin-Token";

    private final AdminTokenStore tokenStore;

    public AdminAuthInterceptor(AdminTokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 浏览器的跨域预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader(TOKEN_HEADER);
        AdminTokenStore.Session session = tokenStore.get(token);
        if (session == null) {
            writeUnauthorized(response);
            return false;
        }

        // 后面如果需要知道「是谁在操作」，直接从 request 里取
        request.setAttribute("adminSession", session);
        return true;
    }

    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"登录已过期，请重新登录后台~\",\"data\":null}");
        response.getWriter().flush();
    }
}
