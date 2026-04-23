package com.gdut.secondhand.service;

import com.gdut.secondhand.dto.request.UserUpdateRequest;
import com.gdut.secondhand.dto.response.LoginResponse;
import com.gdut.secondhand.entity.User;
import com.gdut.secondhand.exception.BusinessException;
import com.gdut.secondhand.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public LoginResponse.UserInfo getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw BusinessException.of("用户不存在");
        }
        return LoginResponse.UserInfo.fromUser(user);
    }

    public User getCurrentUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw BusinessException.of("用户不存在");
        }
        return user;
    }

    @Transactional
    public LoginResponse.UserInfo update(Long id, UserUpdateRequest request) {
        User user = getCurrentUser(id);

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        userMapper.update(user);

        return LoginResponse.UserInfo.fromUser(user);
    }
}