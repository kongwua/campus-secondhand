package com.gdut.secondhand.controller;

import com.gdut.secondhand.dto.request.ReviewCreateRequest;
import com.gdut.secondhand.dto.response.ApiResponse;
import com.gdut.secondhand.dto.response.ReviewResponse;
import com.gdut.secondhand.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@Valid @RequestBody ReviewCreateRequest request) {
        Long userId = UserContext.getCurrentUserId();
        Long reviewId = reviewService.create(request, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("id", reviewId);
        return ApiResponse.success("评价成功", result);
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<ReviewResponse>> getReviewsByUserId(@PathVariable Long userId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByUserId(userId);
        return ApiResponse.success(reviews);
    }

    @GetMapping("/my")
    public ApiResponse<List<ReviewResponse>> getMyReviews() {
        Long userId = UserContext.getCurrentUserId();
        List<ReviewResponse> reviews = reviewService.getMyReviews(userId);
        return ApiResponse.success(reviews);
    }
}