package com.campus.secondhand.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private Long transactionId;
    private Long reviewerId;
    private String reviewerNickname;
    private String reviewerAvatar;
    private Long reviewedId;
    private String reviewedNickname;
    private Integer rating;
    private String content;
    private LocalDateTime createTime;
}