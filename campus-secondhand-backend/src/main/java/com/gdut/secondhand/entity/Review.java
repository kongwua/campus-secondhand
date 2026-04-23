package com.gdut.secondhand.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Review {
    private Long id;
    private Long transactionId;
    private Long reviewerId;
    private Long reviewedId;
    private Integer rating;
    private String content;
    private LocalDateTime createTime;
}
