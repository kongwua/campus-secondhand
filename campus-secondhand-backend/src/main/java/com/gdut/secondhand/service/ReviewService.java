package com.gdut.secondhand.service;

import com.gdut.secondhand.dto.request.ReviewCreateRequest;
import com.gdut.secondhand.dto.response.ReviewResponse;
import com.gdut.secondhand.entity.Review;
import com.gdut.secondhand.entity.Transaction;
import com.gdut.secondhand.entity.User;
import com.gdut.secondhand.exception.BusinessException;
import com.gdut.secondhand.mapper.ReviewMapper;
import com.gdut.secondhand.mapper.TransactionMapper;
import com.gdut.secondhand.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final TransactionMapper transactionMapper;
    private final UserMapper userMapper;

    @Transactional
    public Long create(ReviewCreateRequest request, Long reviewerId) {
        Transaction transaction = transactionMapper.selectById(request.getTransactionId());
        if (transaction == null) {
            throw BusinessException.of(404, "交易不存在");
        }

        if (transaction.getStatus() != TransactionService.STATUS_COMPLETED) {
            throw BusinessException.of(400, "只能对已完成的交易进行评价");
        }

        // Verify reviewer is part of the transaction (either buyer or seller)
        if (!transaction.getSellerId().equals(reviewerId) && !transaction.getBuyerId().equals(reviewerId)) {
            throw BusinessException.of(403, "只有交易参与者才能评价");
        }

        // Check if already reviewed
        Review existingReview = reviewMapper.selectByTransactionId(request.getTransactionId());
        if (existingReview != null) {
            throw BusinessException.of(400, "该交易已被评价");
        }

        // Determine who is being reviewed (the other party in the transaction)
        Long reviewedId = transaction.getSellerId().equals(reviewerId) ? transaction.getBuyerId() : transaction.getSellerId();

        Review review = new Review();
        review.setTransactionId(request.getTransactionId());
        review.setReviewerId(reviewerId);
        review.setReviewedId(reviewedId);
        review.setRating(request.getRating());
        review.setContent(request.getContent());

        reviewMapper.insert(review);

        // Update credit score of the reviewed user
        updateCreditScore(reviewedId, request.getRating());

        return review.getId();
    }

    public List<ReviewResponse> getReviewsByUserId(Long userId) {
        List<Review> reviews = reviewMapper.selectByReviewedId(userId);
        List<ReviewResponse> responses = new ArrayList<>();
        for (Review review : reviews) {
            responses.add(buildReviewResponse(review));
        }
        return responses;
    }

    public List<ReviewResponse> getMyReviews(Long reviewerId) {
        List<Review> reviews = reviewMapper.selectByReviewerId(reviewerId);
        List<ReviewResponse> responses = new ArrayList<>();
        for (Review review : reviews) {
            responses.add(buildReviewResponse(review));
        }
        return responses;
    }

    private void updateCreditScore(Long userId, Integer rating) {
        User user = userMapper.selectById(userId);
        if (user == null) return;

        int currentScore = user.getCreditScore() != null ? user.getCreditScore() : 100;
        int change;

        // 4-5 stars: +2, 1-3 stars: -1
        if (rating >= 4) {
            change = 2;
        } else {
            change = -1;
        }

        int newScore = Math.max(0, Math.min(200, currentScore + change));
        user.setCreditScore(newScore);
        userMapper.updateCreditScore(userId, newScore);
    }

    private ReviewResponse buildReviewResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setTransactionId(review.getTransactionId());
        response.setReviewerId(review.getReviewerId());
        response.setReviewedId(review.getReviewedId());
        response.setRating(review.getRating());
        response.setContent(review.getContent());
        response.setCreateTime(review.getCreateTime());

        User reviewer = userMapper.selectById(review.getReviewerId());
        if (reviewer != null) {
            response.setReviewerNickname(reviewer.getNickname() != null ? reviewer.getNickname() : reviewer.getUsername());
            response.setReviewerAvatar(reviewer.getAvatarUrl());
        }

        User reviewed = userMapper.selectById(review.getReviewedId());
        if (reviewed != null) {
            response.setReviewedNickname(reviewed.getNickname() != null ? reviewed.getNickname() : reviewed.getUsername());
        }

        return response;
    }
}