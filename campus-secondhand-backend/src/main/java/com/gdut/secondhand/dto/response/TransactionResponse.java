package com.gdut.secondhand.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private Long id;
    private Long productId;
    private String productTitle;
    private String productImage;
    private Long sellerId;
    private String sellerNickname;
    private String sellerAvatar;
    private Long buyerId;
    private String buyerNickname;
    private String buyerAvatar;
    private BigDecimal price;
    private Byte status;
    private String statusText;
    private LocalDateTime meetingTime;
    private String meetingLocation;
    private LocalDateTime createTime;
    private LocalDateTime completeTime;
    private Boolean canReview;
    
    public String getStatusText() {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待确认";
            case 1 -> "进行中";
            case 2 -> "已完成";
            case 3 -> "已取消";
            default -> "未知";
        };
    }
}