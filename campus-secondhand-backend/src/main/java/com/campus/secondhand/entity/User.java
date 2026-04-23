package com.campus.secondhand.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private String email;
    private Integer creditScore;
    private Byte status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
