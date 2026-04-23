package com.campus.secondhand.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDetailResponse {
    private Long id;
    private Long userId;
    private String username;
    private String userAvatar;
    private String title;
    private String description;
    private Integer categoryId;
    private String categoryName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private List<String> images;
    private String location;
    private Byte status;
    private Integer viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}