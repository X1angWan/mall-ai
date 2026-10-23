package com.example.mall.common;

import lombok.Getter;

/**
 * 业务异常：Service 层抛出，由全局异常处理器收敛为统一返回体
 */
@Getter
public class BusinessException extends RuntimeException {

    private final IErrorCode errorCode;

    public BusinessException(IErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /** 支持覆盖默认错误信息，例如拼接具体业务上下文 */
    public BusinessException(IErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
