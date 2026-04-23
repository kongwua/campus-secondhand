package com.gdut.secondhand.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private Integer categoryId;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String images;
    private String location;
    private Byte status;
    private Integer viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
