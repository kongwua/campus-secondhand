package com.campus.secondhand.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Transaction {
    private Long id;
    private Long productId;
    private Long sellerId;
    private Long buyerId;
    private BigDecimal price;
    private Byte status;
    private LocalDateTime meetingTime;
    private String meetingLocation;
    private LocalDateTime createTime;
    private LocalDateTime completeTime;
}
