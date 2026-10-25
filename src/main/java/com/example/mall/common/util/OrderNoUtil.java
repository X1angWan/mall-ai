package com.example.mall.common.util;

import cn.hutool.core.util.RandomUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 订单号生成工具：ORD + yyyyMMddHHmmss + 6 位随机数字。
 * <p>唯一性由 t_order.uk_order_no 唯一索引兜底（同秒 + 撞 6 位随机数的概率约百万分之一每秒）。</p>
 */
public final class OrderNoUtil {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private OrderNoUtil() {
    }

    /**
     * 生成一个新订单号。
     *
     * @return 形如 ORD202610252312018372615 的订单号
     */
    public static String generate() {
        return "ORD" + LocalDateTime.now().format(TS) + RandomUtil.randomNumbers(6);
    }
}
