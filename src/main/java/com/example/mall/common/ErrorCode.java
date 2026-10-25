package com.example.mall.common;

/**
 * 全局错误码：通用错误码对齐 HTTP 语义，业务错误码从 10000 起（后续模块按需追加）
 */
public enum ErrorCode implements IErrorCode {

    /** 成功 */
    SUCCESS(200, "成功"),

    /** 通用错误 */
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或凭证已失效"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    SYSTEM_ERROR(500, "系统繁忙，请稍后重试"),

    /** 通用业务错误（细化错误码由各模块追加，从 10001 起） */
    BUSINESS_ERROR(10000, "业务处理失败"),

    /** 商品模块 */
    PRODUCT_NOT_FOUND(10001, "商品不存在"),
    PRODUCT_OFF_SHELF(10002, "商品已下架"),
    STOCK_NOT_ENOUGH(10003, "库存不足"),

    /** 订单模块 */
    ORDER_NOT_FOUND(10010, "订单不存在"),
    ORDER_STATUS_INVALID(10011, "订单状态不允许该操作"),
    DUPLICATE_REQUEST(10012, "重复请求，请勿重复提交");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
