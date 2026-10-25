package com.example.mall.module.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细出参（下单时刻的商品快照）
 */
@Data
public class OrderItemVO {

    private Long id;
    private Long productId;
    private String productName;
    private String productImage;

    /** 成交单价快照 */
    private BigDecimal price;

    /** 购买数量 */
    private Integer quantity;

    /** 小计 = price × quantity */
    private BigDecimal subtotal;
}
