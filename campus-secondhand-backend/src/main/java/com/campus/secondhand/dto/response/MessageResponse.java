package com.campus.secondhand.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private Long id;
    private Long senderId;
    private String senderNickname;
    private String senderAvatar;
    private Long receiverId;
    private String receiverNickname;
    private String receiverAvatar;
    private Long productId;
    private String productTitle;
    private String content;
    private Byte isRead;
    private LocalDateTime createTime;
}