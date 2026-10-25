package com.example.mall.module.order.enums;

import lombok.Getter;

/**
 * 订单状态枚举：code 落库（t_order.status），desc 用于出参展示。
 * <p>合法流转见 OrderMapper 的条件更新方法：
 * 10→20 支付、10→50 取消/关单、20→50 取消（模拟退款）、20→30 发货、30→40 确认收货。</p>
 */
@Getter
public enum OrderStatus {

    /** 待支付 */
    PENDING_PAYMENT(10, "待支付"),
    /** 已支付 */
    PAID(20, "已支付"),
    /** 已发货 */
    SHIPPED(30, "已发货"),
    /** 已完成 */
    COMPLETED(40, "已完成"),
    /** 已取消 */
    CANCELLED(50, "已取消");

    private final int code;
    private final String desc;

    OrderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 按 code 找枚举，找不到返回 null（调用方自行决定按异常处理）。
     *
     * @param code 状态码（落库值）
     * @return 对应枚举，或 null
     */
    public static OrderStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
