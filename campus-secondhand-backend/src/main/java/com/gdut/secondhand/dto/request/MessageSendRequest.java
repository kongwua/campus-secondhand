package com.gdut.secondhand.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MessageSendRequest {
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    private Long productId;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 1000, message = "消息内容最多1000个字符")
    private String content;
}