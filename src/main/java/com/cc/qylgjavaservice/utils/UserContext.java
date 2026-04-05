package com.cc.qylgjavaservice.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class UserContext {
    public static Long getCurrentUserId() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        Object userIdObj = request.getAttribute("CURRENT_USER_ID");
        return userIdObj != null ? (Long) userIdObj : null;
    }

    public static String getToken() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        Object userIdObj = request.getAttribute("TOKEN");
        return userIdObj != null ? (String) userIdObj : null;
    }
}