package com.pedro.finance_control.response;

import io.swagger.v3.core.jackson.ApiResponsesSerializer;

public record ApiResponse<T>(boolean success, T data, String message) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, "Success");
    }

    public static <T> ApiResponse<T> success(T data, String message){
        return new ApiResponse<>(true, data, message);
    }

    public static <T> ApiResponse<T> error(String message){
        return new ApiResponse<>(false, null, message);
    }

}
