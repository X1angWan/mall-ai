package com.example.mall.common;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回体：HTTP 恒为 200，业务成败由 code 区分
 */
@Data
@JsonPropertyOrder({"code", "message", "data"})
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok() {
        return build(ErrorCode.SUCCESS, null);
    }

    public static <T> Result<T> ok(T data) {
        return build(ErrorCode.SUCCESS, data);
    }

    public static <T> Result<T> fail(IErrorCode errorCode) {
        return build(errorCode, null);
    }

    public static <T> Result<T> fail(IErrorCode errorCode, String message) {
        Result<T> result = build(errorCode, null);
        result.setMessage(message);
        return result;
    }

    private static <T> Result<T> build(IErrorCode errorCode, T data) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMessage(errorCode.getMessage());
        result.setData(data);
        return result;
    }
}
