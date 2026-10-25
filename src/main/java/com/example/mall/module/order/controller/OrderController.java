package com.example.mall.module.order.controller;

import com.example.mall.common.PageResult;
import com.example.mall.common.Result;
import com.example.mall.module.order.dto.OrderCreateDTO;
import com.example.mall.module.order.dto.OrderQueryDTO;
import com.example.mall.module.order.dto.OrderVO;
import com.example.mall.module.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单接口（08 章加登录校验）
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 下单（幂等）：同 requestId 重复提交返回同一订单。
     */
    @PostMapping
    public Result<OrderVO> placeOrder(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.ok(orderService.placeOrder(dto));
    }

    /**
     * 模拟支付：待支付 → 已支付。
     */
    @PostMapping("/{orderNo}/pay")
    public Result<OrderVO> pay(@PathVariable String orderNo) {
        return Result.ok(orderService.pay(orderNo));
    }

    /**
     * 取消订单：待支付直接取消；已支付取消并模拟退款；均回补库存。
     */
    @PostMapping("/{orderNo}/cancel")
    public Result<OrderVO> cancel(@PathVariable String orderNo) {
        return Result.ok(orderService.cancel(orderNo));
    }

    /**
     * 订单详情（主单 + 明细）。
     */
    @GetMapping("/{orderNo}")
    public Result<OrderVO> detail(@PathVariable String orderNo) {
        return Result.ok(orderService.getDetail(orderNo));
    }

    /**
     * 订单分页查询：GET /api/orders/page?userId=1&status=10&pageNum=1&pageSize=10
     */
    @GetMapping("/page")
    public Result<PageResult<OrderVO>> page(OrderQueryDTO query) {
        return Result.ok(orderService.page(query));
    }
}
