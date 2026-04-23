package com.campus.secondhand.mapper;

import com.campus.secondhand.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TransactionMapper {

    int insert(Transaction transaction);

    Transaction selectById(@Param("id") Long id);

    List<Transaction> selectByUserId(@Param("userId") Long userId, @Param("role") String role);

    List<Transaction> selectByProductId(@Param("productId") Long productId);

    int updateStatus(@Param("id") Long id, @Param("status") Byte status);

    int updateComplete(@Param("id") Long id, @Param("status") Byte status);

    Transaction selectByProductIdAndStatus(@Param("productId") Long productId, @Param("status") Byte status);

    int countByProductIdAndStatusIn(@Param("productId") Long productId, @Param("statuses") List<Byte> statuses);
}