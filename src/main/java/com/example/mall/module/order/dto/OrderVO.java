package com.example.mall.module.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单出参：主单字段 + 明细列表 + statusDesc 中文描述
 */
@Data
public class OrderVO {

    private Long id;

    /** 订单号 */
    private String orderNo;

    private Long userId;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 商品总件数 */
    private Integer totalQuantity;

    /** 状态码：10/20/30/40/50 */
    private Integer status;

    /** 状态中文描述（待支付/已支付/...），由 OrderStatus 枚举翻译 */
    private String statusDesc;

    private String remark;

    private LocalDateTime createTime;

    /** 明细列表 */
    private List<OrderItemVO> items;
}
