package com.example.mall.common;

/**
 * 错误码接口：枚举实现它即可被 Result / BusinessException 复用
 */
public interface IErrorCode {

    int getCode();

    String getMessage();
}
