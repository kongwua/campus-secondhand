package com.gdut.secondhand.controller;

import com.gdut.secondhand.exception.BusinessException;

public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    public static void setCurrentUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getCurrentUserId() {
        Long userId = USER_ID.get();
        if (userId == null) {
            throw BusinessException.of(401, "未登录");
        }
        return userId;
    }

    public static void clear() {
        USER_ID.remove();
    }
}