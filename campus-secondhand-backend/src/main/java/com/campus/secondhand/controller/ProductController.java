package com.campus.secondhand.controller;

import com.campus.secondhand.dto.request.ProductCreateRequest;
import com.campus.secondhand.dto.request.ProductQueryRequest;
import com.campus.secondhand.dto.response.ApiResponse;
import com.campus.secondhand.dto.response.PageResult;
import com.campus.secondhand.dto.response.ProductDetailResponse;
import com.campus.secondhand.entity.Category;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.service.ObsService;
import com.campus.secondhand.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ObsService obsService;

    @GetMapping
    public ApiResponse<PageResult<Product>> list(ProductQueryRequest request) {
        PageResult<Product> result = productService.list(request);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDetailResponse> getById(@PathVariable Long id) {
        ProductDetailResponse response = productService.getById(id);
        return ApiResponse.success(response);
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@Valid @RequestBody ProductCreateRequest request) {
        Long userId = UserContext.getCurrentUserId();
        Long productId = productService.create(request, userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", productId);
        return ApiResponse.success("商品发布成功", result);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody ProductCreateRequest request) {
        Long userId = UserContext.getCurrentUserId();
        productService.update(id, request, userId);
        return ApiResponse.success("商品更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        productService.delete(id, userId);
        return ApiResponse.success("商品已下架");
    }

    @GetMapping("/categories")
    public ApiResponse<List<Category>> getCategories() {
        List<Category> categories = productService.getCategories();
        return ApiResponse.success(categories);
    }

    @GetMapping("/search")
    public ApiResponse<PageResult<Product>> search(ProductQueryRequest request) {
        PageResult<Product> result = productService.search(request);
        return ApiResponse.success(result);
    }

    @GetMapping("/upload-url")
    public ApiResponse<Map<String, String>> getUploadUrl(@RequestParam(required = false) String filename) {
        Long userId = UserContext.getCurrentUserId();
        
        String objectKey = obsService.generateObjectKey(filename);
        String uploadUrl = obsService.generatePresignedPutUrl(objectKey);
        
        Map<String, String> result = new HashMap<>();
        result.put("objectKey", objectKey);
        result.put("uploadUrl", uploadUrl);
        return ApiResponse.success(result);
    }
}