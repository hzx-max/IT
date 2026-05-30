package com.netconfig.dto;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private boolean ok;
    private T data;
    private String error;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setOk(true);
        r.setData(data);
        return r;
    }

    public static <T> ApiResponse<T> success() {
        ApiResponse<T> r = new ApiResponse<>();
        r.setOk(true);
        return r;
    }

    public static <T> ApiResponse<T> error(String error) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setOk(false);
        r.setError(error);
        return r;
    }
}
