package com.gdut.secondhand.mapper;

import com.gdut.secondhand.dto.request.ProductQueryRequest;
import com.gdut.secondhand.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    int insert(Product product);

    Product selectById(@Param("id") Long id);

    List<Product> selectPage(ProductQueryRequest request);

    Long countPage(ProductQueryRequest request);

    int update(Product product);

    int delete(@Param("id") Long id);

    int increaseViewCount(@Param("id") Long id);
}