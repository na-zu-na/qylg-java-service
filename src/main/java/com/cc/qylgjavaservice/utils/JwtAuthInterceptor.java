package com.cc.qylgjavaservice.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.cc.qylgjavaservice.utils.RedisConstants.*;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            sendErrorResponse(response, 401, "MISSING_TOKEN", "未提供有效的 Authorization Token");
            return false;
        }

        String token = authorization.substring(7);

        String blacklistKey = BLACKLIST_PREFIX + token;
        String adminKey = ADMIN_TOKEN_KEY + token;
        String userKey = USER_TOKEN_KEY + token;

        RBucket<String> blacklistBucket = redissonClient.getBucket(blacklistKey);
        RBucket<Object> adminBucket = redissonClient.getBucket(adminKey);
        RBucket<Object> userBucket = redissonClient.getBucket(userKey);

        // 1. 先检查黑名单
        if (blacklistBucket.isExists()) {
            sendErrorResponse(response, 401, "TOKEN_INVALID", "Token 已失效");
            return false;
        }

        // 2. 校验 JWT 本身
        if (!jwtUtil.validateToken(token)) {
            sendErrorResponse(response, 401, "TOKEN_INVALID", "Token 无效或已过期");
            return false;
        }

        // 3. 校验 Redis 登录态：管理员或用户任意一种存在即可
        boolean adminExists = adminBucket.isExists();
        boolean userExists = userBucket.isExists();

        if (!adminExists && !userExists) {
            sendErrorResponse(response, 401, "TOKEN_EXPIRED", "登录状态已失效");
            return false;
        }

        // 4. 获取用户信息
        Long userId = jwtUtil.getUserId(token);
        request.setAttribute("CURRENT_USER_ID", userId);

        // 5. 刷新 Redis 过期时间（滑动过期）
        if (adminExists) {
            adminBucket.expire(60, TimeUnit.MINUTES);
        }
        if (userExists) {
            userBucket.expire(60, TimeUnit.MINUTES);
        }

        return true;
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String code, String message) throws IOException {
        // 设置状态码
        response.setStatus(status);

        // 设置 Content-Type 为 JSON
        response.setContentType("application/json;charset=UTF-8");

        // 处理跨域头
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // 构建统一的错误 JSON 结构
        String jsonResponse = String.format(
                "{\"code\": %d, \"errorCode\": \"%s\", \"message\": \"%s\", \"data\": null}",
                status, code, message
        );

        // 5. 写入输出流
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}
