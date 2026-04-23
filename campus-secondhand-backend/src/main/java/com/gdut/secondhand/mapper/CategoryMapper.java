package com.gdut.secondhand.mapper;

import com.gdut.secondhand.entity.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {

    List<Category> selectAll();

    Category selectById(Integer id);
}