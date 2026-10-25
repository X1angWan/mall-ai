package com.example.mall.module.order.service;

import com.example.mall.common.PageResult;
import com.example.mall.module.order.dto.OrderCreateDTO;
import com.example.mall.module.order.dto.OrderQueryDTO;
import com.example.mall.module.order.dto.OrderVO;

/**
 * 订单 Service
 */
public interface OrderService {

    /**
     * 下单（幂等）：校验商品 → 原子扣库存 → 落主单与快照明细，全程一个事务。
     * 同 requestId 的重复请求返回已有订单，不重复扣库存。
     *
     * @param dto 下单入参
     * @return 订单 VO（含明细与状态中文描述）
     */
    OrderVO placeOrder(OrderCreateDTO dto);

    /**
     * 模拟支付：待支付(10) → 已支付(20)，条件更新保证并发下只有一次成功。
     *
     * @param orderNo 订单号
     * @return 支付后的订单 VO
     */
    OrderVO pay(String orderNo);

    /**
     * 取消订单（定稿 v2 两条路径）：待支付(10)取消；已支付(20)取消并模拟退款。
     * 成功后同事务逐明细回补库存。
     *
     * @param orderNo 订单号
     * @return 取消后的订单 VO
     */
    OrderVO cancel(String orderNo);

    /**
     * 订单详情（主单 + 明细）。
     *
     * @param orderNo 订单号
     * @return 订单 VO
     */
    OrderVO getDetail(String orderNo);

    /**
     * 订单分页查询（可按 userId / status 过滤，按创建时间倒序）。列表不带明细。
     *
     * @param query 分页与过滤条件
     * @return 分页结果
     */
    PageResult<OrderVO> page(OrderQueryDTO query);
}
