package com.gdut.secondhand.service;

import com.gdut.secondhand.dto.request.ProductCreateRequest;
import com.gdut.secondhand.dto.request.ProductQueryRequest;
import com.gdut.secondhand.dto.response.PageResult;
import com.gdut.secondhand.dto.response.ProductDetailResponse;
import com.gdut.secondhand.entity.Category;
import com.gdut.secondhand.entity.Product;
import com.gdut.secondhand.entity.User;
import com.gdut.secondhand.exception.BusinessException;
import com.gdut.secondhand.mapper.CategoryMapper;
import com.gdut.secondhand.mapper.ProductMapper;
import com.gdut.secondhand.mapper.UserMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private static final byte STATUS_ACTIVE = 1;
    
    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public PageResult<Product> list(ProductQueryRequest request) {
        if (request.getPageNum() == null || request.getPageNum() < 1) {
            request.setPageNum(1);
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(10);
        }
        
        List<Product> products = productMapper.selectPage(request);
        Long total = productMapper.countPage(request);
        
        return PageResult.of(products, total, request.getPageNum(), request.getPageSize());
    }

    public PageResult<Product> search(ProductQueryRequest request) {
        return list(request);
    }

    @Transactional
    public ProductDetailResponse getById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw BusinessException.of(404, "商品不存在");
        }
        
        productMapper.increaseViewCount(id);
        
        ProductDetailResponse response = new ProductDetailResponse();
        response.setId(product.getId());
        response.setUserId(product.getUserId());
        response.setTitle(product.getTitle());
        response.setDescription(product.getDescription());
        response.setCategoryId(product.getCategoryId());
        response.setPrice(product.getPrice());
        response.setOriginalPrice(product.getOriginalPrice());
        response.setLocation(product.getLocation());
        response.setStatus(product.getStatus());
        response.setViewCount(product.getViewCount() + 1);
        response.setCreateTime(product.getCreateTime());
        response.setUpdateTime(product.getUpdateTime());
        response.setImages(parseImages(product.getImages()));
        
        Category category = categoryMapper.selectById(product.getCategoryId());
        if (category != null) {
            response.setCategoryName(category.getName());
        }
        
        User user = userMapper.selectById(product.getUserId());
        if (user != null) {
            response.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
            response.setUserAvatar(user.getAvatarUrl());
        }
        
        return response;
    }

    @Transactional
    public Long create(ProductCreateRequest request, Long userId) {
        Category category = categoryMapper.selectById(request.getCategoryId());
        if (category == null) {
            throw BusinessException.of(400, "分类不存在");
        }
        
        Product product = new Product();
        product.setUserId(userId);
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setCategoryId(request.getCategoryId());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setImages(serializeImages(request.getImages()));
        product.setLocation(request.getLocation());
        product.setStatus(STATUS_ACTIVE);
        product.setViewCount(0);
        
        productMapper.insert(product);
        return product.getId();
    }

    @Transactional
    public void update(Long id, ProductCreateRequest request, Long userId) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw BusinessException.of(404, "商品不存在");
        }
        
        if (!product.getUserId().equals(userId)) {
            throw BusinessException.of(403, "无权修改此商品");
        }
        
        if (request.getCategoryId() != null) {
            Category category = categoryMapper.selectById(request.getCategoryId());
            if (category == null) {
                throw BusinessException.of(400, "分类不存在");
            }
        }
        
        if (request.getTitle() != null) {
            product.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getCategoryId() != null) {
            product.setCategoryId(request.getCategoryId());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getOriginalPrice() != null) {
            product.setOriginalPrice(request.getOriginalPrice());
        }
        if (request.getImages() != null) {
            product.setImages(serializeImages(request.getImages()));
        }
        if (request.getLocation() != null) {
            product.setLocation(request.getLocation());
        }
        
        productMapper.update(product);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw BusinessException.of(404, "商品不存在");
        }
        
        if (!product.getUserId().equals(userId)) {
            throw BusinessException.of(403, "无权删除此商品");
        }
        
        productMapper.delete(id);
    }

    public void increaseViewCount(Long id) {
        productMapper.increaseViewCount(id);
    }

    public List<Category> getCategories() {
        return categoryMapper.selectAll();
    }

    private List<String> parseImages(String imagesJson) {
        if (imagesJson == null || imagesJson.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse images JSON: {}", imagesJson, e);
            return Collections.emptyList();
        }
    }

    private String serializeImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(images);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize images", e);
            return null;
        }
    }
}