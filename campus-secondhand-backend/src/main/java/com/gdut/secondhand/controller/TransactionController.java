package com.gdut.secondhand.controller;

import com.gdut.secondhand.dto.request.TransactionCreateRequest;
import com.gdut.secondhand.dto.response.ApiResponse;
import com.gdut.secondhand.dto.response.TransactionResponse;
import com.gdut.secondhand.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@Valid @RequestBody TransactionCreateRequest request) {
        Long userId = UserContext.getCurrentUserId();
        Long transactionId = transactionService.create(request, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("id", transactionId);
        return ApiResponse.success("交易创建成功", result);
    }

    @GetMapping("/{id}")
    public ApiResponse<TransactionResponse> getById(@PathVariable Long id) {
        TransactionResponse response = transactionService.getById(id);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}/confirm")
    public ApiResponse<Void> confirm(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        transactionService.confirm(id, userId);
        return ApiResponse.success("交易已确认");
    }

    @PutMapping("/{id}/complete")
    public ApiResponse<Void> complete(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        transactionService.complete(id, userId);
        return ApiResponse.success("交易已完成");
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        transactionService.cancel(id, userId);
        return ApiResponse.success("交易已取消");
    }

    @GetMapping("/my")
    public ApiResponse<List<TransactionResponse>> getMyTransactions(@RequestParam String role) {
        Long userId = UserContext.getCurrentUserId();
        List<TransactionResponse> transactions = transactionService.getMyTransactions(userId, role);
        return ApiResponse.success(transactions);
    }
}