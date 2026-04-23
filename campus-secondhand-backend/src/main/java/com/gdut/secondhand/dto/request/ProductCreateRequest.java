package com.gdut.secondhand.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductCreateRequest {
    @NotBlank(message = "商品标题不能为空")
    @Size(max = 100, message = "标题最多100个字符")
    private String title;

    @Size(max = 2000, message = "描述最多2000个字符")
    private String description;

    @NotNull(message = "分类不能为空")
    private Integer categoryId;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "原价必须大于0")
    private BigDecimal originalPrice;

    @Size(max = 9, message = "最多上传9张图片")
    private List<String> images;

    @Size(max = 100, message = "位置最多100个字符")
    private String location;
}