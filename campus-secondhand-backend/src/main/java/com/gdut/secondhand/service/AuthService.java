package com.gdut.secondhand.service;

import com.gdut.secondhand.dto.request.LoginRequest;
import com.gdut.secondhand.dto.request.RegisterRequest;
import com.gdut.secondhand.dto.response.LoginResponse;
import com.gdut.secondhand.entity.User;
import com.gdut.secondhand.exception.BusinessException;
import com.gdut.secondhand.mapper.UserMapper;
import com.gdut.secondhand.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public void register(RegisterRequest request) {
        User existingUser = userMapper.selectByUsername(request.getUsername());
        if (existingUser != null) {
            throw BusinessException.of("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setCreditScore(100);
        user.setStatus((byte) 0);

        userMapper.insert(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw BusinessException.of("用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw BusinessException.of("用户名或密码错误");
        }

        if (user.getStatus() != 0) {
            throw BusinessException.of("账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .user(LoginResponse.UserInfo.fromUser(user))
                .build();
    }

    public void logout() {
    }
}