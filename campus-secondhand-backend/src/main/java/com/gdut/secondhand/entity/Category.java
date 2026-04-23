package com.gdut.secondhand.entity;

import lombok.Data;

@Data
public class Category {
    private Integer id;
    private String name;
    private Integer parentId;
    private Integer sortOrder;
}
