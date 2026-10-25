package com.example.mall.module.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.module.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 订单 Mapper：状态流转全部用「条件更新」（WHERE status=期望值）实现并发安全——
 * 两个并发请求同时流转同一订单，数据库只放行一次，失败方拿到 0 行。
 * ⚠ 禁止改成「先查 status 再 update」的两步写法，两步之间状态可能被并发修改。
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 支付：status 10 → 20。
     *
     * @param orderId 订单 id
     * @return 影响行数，0 表示当前状态不是待支付（已被并发支付/取消/关单）
     */
    @Update("UPDATE t_order SET status = 20 WHERE id = #{orderId} AND status = 10 AND deleted = 0")
    int updateStatusToPaid(@Param("orderId") Long orderId);

    /**
     * 取消：status → 50，期望状态由调用方传入。
     * <p>支持两条路径：待支付取消（expect=10）与已支付取消+模拟退款（expect=20，定稿 v2）。</p>
     *
     * @param orderId      订单 id
     * @param expectStatus 期望的当前状态（10 或 20）
     * @return 影响行数，0 表示当前状态与期望不符（流转不合法或已被并发处理）
     */
    @Update("UPDATE t_order SET status = 50 WHERE id = #{orderId} AND status = #{expectStatus} AND deleted = 0")
    int updateStatusToCancelled(@Param("orderId") Long orderId, @Param("expectStatus") Integer expectStatus);

    /**
     * 发货：status 20 → 30。
     *
     * @param orderId 订单 id
     * @return 影响行数，0 表示当前状态不是已支付
     */
    @Update("UPDATE t_order SET status = 30 WHERE id = #{orderId} AND status = 20 AND deleted = 0")
    int updateStatusToShipped(@Param("orderId") Long orderId);

    /**
     * 确认收货：status 30 → 40。
     *
     * @param orderId 订单 id
     * @return 影响行数，0 表示当前状态不是已发货
     */
    @Update("UPDATE t_order SET status = 40 WHERE id = #{orderId} AND status = 30 AND deleted = 0")
    int updateStatusToCompleted(@Param("orderId") Long orderId);
}
