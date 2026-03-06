package com.cc.qylgjavaservice.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

import static com.cc.qylgjavaservice.utils.RedisConstants.BLACKLIST_PREFIX;

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
            sendErrorResponse(response,401,"MISSING_TOKEN","未提供有效的 Authorization Token");
            return false; // 未携带 Token
        }

        String token = authorization.substring(7);

        //查询黑名单
        String key=BLACKLIST_PREFIX+token;
        RBucket<String> bucket=redissonClient.getBucket(key);

        // 校验 Token 和 黑名单
        if (!jwtUtil.validateToken(token) || bucket.isExists()) {
            sendErrorResponse(response, 401, "TOKEN_INVALID", "Token 无效或已过期");
            return false; // Token 无效或过期
        }

        Long userId=jwtUtil.getUserId(token);
        request.setAttribute("CURRENT_USER_ID", userId);
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
