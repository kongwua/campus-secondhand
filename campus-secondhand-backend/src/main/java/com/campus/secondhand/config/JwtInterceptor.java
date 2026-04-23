package com.campus.secondhand.config;

import com.campus.secondhand.controller.UserContext;
import com.campus.secondhand.exception.BusinessException;
import com.campus.secondhand.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (isPublicGetRequest(request)) {
            return true;
        }
        
        String token = extractToken(request);

        if (!StringUtils.hasText(token)) {
            throw BusinessException.of(401, "未登录");
        }

        if (!jwtUtil.validateToken(token)) {
            throw BusinessException.of(401, "登录已过期，请重新登录");
        }

        Long userId = jwtUtil.getUserId(token);
        UserContext.setCurrentUserId(userId);

        return true;
    }

    private boolean isPublicGetRequest(HttpServletRequest request) {
        if (!"GET".equals(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        return path.startsWith("/api/products") || path.equals("/api/products/categories");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}