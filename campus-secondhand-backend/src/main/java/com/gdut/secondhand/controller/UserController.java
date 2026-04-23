package com.gdut.secondhand.controller;

import com.gdut.secondhand.dto.request.UserUpdateRequest;
import com.gdut.secondhand.dto.response.ApiResponse;
import com.gdut.secondhand.dto.response.LoginResponse;
import com.gdut.secondhand.entity.User;
import com.gdut.secondhand.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public ApiResponse<LoginResponse.UserInfo> getUserInfo() {
        Long userId = UserContext.getCurrentUserId();
        LoginResponse.UserInfo user = userService.getById(userId);
        return ApiResponse.success(user);
    }

    @PutMapping("/update")
    public ApiResponse<LoginResponse.UserInfo> updateUser(@Valid @RequestBody UserUpdateRequest request) {
        Long userId = UserContext.getCurrentUserId();
        LoginResponse.UserInfo user = userService.update(userId, request);
        return ApiResponse.success("更新成功", user);
    }
}