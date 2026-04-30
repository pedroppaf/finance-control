package com.pedro.finance_control.response;

public record AppApiResponse<T>(boolean success, T data, String message) {

    public static <T> AppApiResponse<T> success(T data) {
        return new AppApiResponse<>(true, data, "Success");
    }

    public static <T> AppApiResponse<T> success(T data, String message){
        return new AppApiResponse<>(true, data, message);
    }

    public static <T> AppApiResponse<T> error(String message){
        return new AppApiResponse<>(false, null, message);
    }

}
