package com.campus.secondhand.mapper;

import com.campus.secondhand.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReviewMapper {

    int insert(Review review);

    Review selectById(@Param("id") Long id);

    Review selectByTransactionId(@Param("transactionId") Long transactionId);

    List<Review> selectByReviewedId(@Param("reviewedId") Long reviewedId);

    List<Review> selectByReviewerId(@Param("reviewerId") Long reviewerId);

    int countByReviewedId(@Param("reviewedId") Long reviewedId);
}