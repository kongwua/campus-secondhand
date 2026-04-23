package com.gdut.secondhand.dto.request;

import lombok.Data;

@Data
public class ProductQueryRequest {
    private String keyword;
    private Integer categoryId;
    private Double minPrice;
    private Double maxPrice;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}