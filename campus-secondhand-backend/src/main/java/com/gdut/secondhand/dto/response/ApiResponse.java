package com.gdut.secondhand.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>(200, "success", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<T>(200, message, data);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<Void>(200, message, null);
    }

    public static ApiResponse<Void> error(int code, String message) {
        return new ApiResponse<Void>(code, message, null);
    }

    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<Void>(500, message, null);
    }
}