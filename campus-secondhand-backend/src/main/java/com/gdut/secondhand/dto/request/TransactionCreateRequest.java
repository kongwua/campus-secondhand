package com.gdut.secondhand.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionCreateRequest {
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    private LocalDateTime meetingTime;

    @Size(max = 200, message = "交易地点最多200个字符")
    private String meetingLocation;
}