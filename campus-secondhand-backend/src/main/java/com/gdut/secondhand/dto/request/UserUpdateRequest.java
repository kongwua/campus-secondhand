package com.gdut.secondhand.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @Size(max = 100, message = "昵称长度不能超过100")
    private String nickname;

    @Size(max = 500, message = "头像URL长度不能超过500")
    private String avatarUrl;

    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    private String email;
}