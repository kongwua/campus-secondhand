package com.campus.secondhand.service;

import com.campus.secondhand.dto.request.TransactionCreateRequest;
import com.campus.secondhand.dto.response.TransactionResponse;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.entity.Transaction;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.exception.BusinessException;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.TransactionMapper;
import com.campus.secondhand.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    // Transaction status constants
    public static final byte STATUS_PENDING = 0;    // 待确认
    public static final byte STATUS_IN_PROGRESS = 1; // 进行中
    public static final byte STATUS_COMPLETED = 2;   // 已完成
    public static final byte STATUS_CANCELLED = 3;   // 已取消

    // Product status constants
    public static final byte PRODUCT_STATUS_ON_SALE = 0;  // 在售
    public static final byte PRODUCT_STATUS_SOLD = 1;     // 已售

    private final TransactionMapper transactionMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    @Transactional
    public Long create(TransactionCreateRequest request, Long buyerId) {
        Product product = productMapper.selectById(request.getProductId());
        if (product == null) {
            throw BusinessException.of(404, "商品不存在");
        }

        if (product.getStatus() != PRODUCT_STATUS_ON_SALE) {
            throw BusinessException.of(400, "商品不在售，无法创建交易");
        }

        if (product.getUserId().equals(buyerId)) {
            throw BusinessException.of(400, "不能购买自己发布的商品");
        }

        // Check if there's already an active transaction for this product
        List<Byte> activeStatuses = new ArrayList<>();
        activeStatuses.add(STATUS_PENDING);
        activeStatuses.add(STATUS_IN_PROGRESS);
        int activeCount = transactionMapper.countByProductIdAndStatusIn(request.getProductId(), activeStatuses);
        if (activeCount > 0) {
            throw BusinessException.of(400, "该商品已有进行中的交易");
        }

        Transaction transaction = new Transaction();
        transaction.setProductId(request.getProductId());
        transaction.setSellerId(product.getUserId());
        transaction.setBuyerId(buyerId);
        transaction.setPrice(request.getPrice());
        transaction.setStatus(STATUS_PENDING);
        transaction.setMeetingTime(request.getMeetingTime());
        transaction.setMeetingLocation(request.getMeetingLocation());

        transactionMapper.insert(transaction);
        return transaction.getId();
    }

    public TransactionResponse getById(Long id) {
        Transaction transaction = transactionMapper.selectById(id);
        if (transaction == null) {
            throw BusinessException.of(404, "交易不存在");
        }
        return buildTransactionResponse(transaction);
    }

    @Transactional
    public void confirm(Long transactionId, Long sellerId) {
        Transaction transaction = transactionMapper.selectById(transactionId);
        if (transaction == null) {
            throw BusinessException.of(404, "交易不存在");
        }

        if (transaction.getStatus() != STATUS_PENDING) {
            throw BusinessException.of(400, "只有待确认状态的交易才能确认");
        }

        if (!transaction.getSellerId().equals(sellerId)) {
            throw BusinessException.of(403, "只有卖家才能确认交易");
        }

        transactionMapper.updateStatus(transactionId, STATUS_IN_PROGRESS);
    }

    @Transactional
    public void complete(Long transactionId, Long operatorId) {
        Transaction transaction = transactionMapper.selectById(transactionId);
        if (transaction == null) {
            throw BusinessException.of(404, "交易不存在");
        }

        if (transaction.getStatus() != STATUS_IN_PROGRESS) {
            throw BusinessException.of(400, "只有进行中的交易才能完成");
        }

        if (!transaction.getSellerId().equals(operatorId) && !transaction.getBuyerId().equals(operatorId)) {
            throw BusinessException.of(403, "只有买卖双方才能完成交易");
        }

        transactionMapper.updateComplete(transactionId, STATUS_COMPLETED);

        Product product = productMapper.selectById(transaction.getProductId());
        if (product != null) {
            product.setStatus(PRODUCT_STATUS_SOLD);
            productMapper.update(product);
        }
    }

    @Transactional
    public void cancel(Long transactionId, Long operatorId) {
        Transaction transaction = transactionMapper.selectById(transactionId);
        if (transaction == null) {
            throw BusinessException.of(404, "交易不存在");
        }

        if (transaction.getStatus() == STATUS_COMPLETED) {
            throw BusinessException.of(400, "已完成的交易不能取消");
        }

        if (transaction.getStatus() == STATUS_CANCELLED) {
            throw BusinessException.of(400, "交易已取消");
        }

        if (!transaction.getSellerId().equals(operatorId) && !transaction.getBuyerId().equals(operatorId)) {
            throw BusinessException.of(403, "只有买卖双方才能取消交易");
        }

        transactionMapper.updateStatus(transactionId, STATUS_CANCELLED);
    }

    public List<TransactionResponse> getMyTransactions(Long userId, String role) {
        List<Transaction> transactions = transactionMapper.selectByUserId(userId, role);
        List<TransactionResponse> responses = new ArrayList<>();
        for (Transaction transaction : transactions) {
            responses.add(buildTransactionResponse(transaction));
        }
        return responses;
    }

    private TransactionResponse buildTransactionResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setProductId(transaction.getProductId());
        response.setPrice(transaction.getPrice());
        response.setStatus(transaction.getStatus());
        response.setMeetingTime(transaction.getMeetingTime());
        response.setMeetingLocation(transaction.getMeetingLocation());
        response.setCreateTime(transaction.getCreateTime());
        response.setCompleteTime(transaction.getCompleteTime());

        Product product = productMapper.selectById(transaction.getProductId());
        if (product != null) {
            response.setProductTitle(product.getTitle());
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                String images = product.getImages();
                if (images.startsWith("[") && images.contains(",")) {
                    response.setProductImage(images.substring(1, images.indexOf(",")).replace("\"", "").trim());
                } else if (images.startsWith("[")) {
                    response.setProductImage(images.substring(1, images.length() - 1).replace("\"", "").trim());
                } else {
                    response.setProductImage(images);
                }
            }
        }

        User seller = userMapper.selectById(transaction.getSellerId());
        if (seller != null) {
            response.setSellerId(seller.getId());
            response.setSellerNickname(seller.getNickname() != null ? seller.getNickname() : seller.getUsername());
            response.setSellerAvatar(seller.getAvatarUrl());
        }

        User buyer = userMapper.selectById(transaction.getBuyerId());
        if (buyer != null) {
            response.setBuyerId(buyer.getId());
            response.setBuyerNickname(buyer.getNickname() != null ? buyer.getNickname() : buyer.getUsername());
            response.setBuyerAvatar(buyer.getAvatarUrl());
        }

        // Can review only if transaction is completed and not yet reviewed
        response.setCanReview(transaction.getStatus() == STATUS_COMPLETED);

        return response;
    }
}