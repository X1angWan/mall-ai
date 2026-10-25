package com.example.mall.module.order.dto;

import lombok.Data;

/**
 * 订单分页查询入参
 */
@Data
public class OrderQueryDTO {

    private long pageNum = 1;
    private long pageSize = 10;

    /** 按用户过滤（演示阶段由入参传入，08 章改为登录态强制本人） */
    private Long userId;

    /** 按状态过滤：10/20/30/40/50 */
    private Integer status;
}
