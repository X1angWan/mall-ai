package com.example.mall.module.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mall.common.BusinessException;
import com.example.mall.common.ErrorCode;
import com.example.mall.common.PageResult;
import com.example.mall.common.util.OrderNoUtil;
import com.example.mall.module.order.dto.OrderCreateDTO;
import com.example.mall.module.order.dto.OrderItemVO;
import com.example.mall.module.order.dto.OrderQueryDTO;
import com.example.mall.module.order.dto.OrderVO;
import com.example.mall.module.order.entity.Order;
import com.example.mall.module.order.entity.OrderItem;
import com.example.mall.module.order.enums.OrderStatus;
import com.example.mall.module.order.mapper.OrderItemMapper;
import com.example.mall.module.order.mapper.OrderMapper;
import com.example.mall.module.order.service.OrderService;
import com.example.mall.module.product.entity.Product;
import com.example.mall.module.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单 Service 实现。
 * <p>结构说明：placeOrder 是无事务入口，doPlaceOrder 才是事务方法，两者分离的原因——
 * 唯一索引冲突（DuplicateKeyException）必须先滚出事务（让本请求已扣的库存随回滚还原），
 * 再在事务外回查赢家订单。若在事务内直接 catch 并返回：① 扣的库存会被提交（白扣）；
 * ② MySQL REPEATABLE READ 下事务内快照看不到赢家（回查返回 null）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;

    /** 自注入代理：内部调用 doPlaceOrder 必须经过代理，@Transactional 才生效（同类直调事务会失效）。
     *  注意必须用 @Lazy 字段注入——final + 构造器注入会形成自引用循环依赖，启动直接失败 */
    @Autowired
    @Lazy
    private OrderServiceImpl self;

    /**
     * 下单入口（无事务）。
     *
     * @param dto 下单入参
     * @return 新订单；或同 requestId 的已有订单（幂等命中）
     */
    @Override
    public OrderVO placeOrder(OrderCreateDTO dto) {
        // 幂等第一层（快速路径）：同 requestId 的订单已存在 → 直接返回原单，不碰库存
        Order existing = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getRequestId, dto.getRequestId()));
        if (existing != null) {
            log.info("幂等命中（快速路径）: requestId={}, orderNo={}", dto.getRequestId(), existing.getOrderNo());
            return toVO(existing, orderItemMapper.selectByOrderId(existing.getId()));
        }
        try {
            // 经代理调用，保证事务注解生效
            return self.doPlaceOrder(dto);
        } catch (DuplicateKeyException e) {
            // 幂等第二层兜底：并发对手已抢先提交，本事务已整体回滚（本请求扣的库存已还原）。
            // 此刻在事务外重查（新快照），一定能看到赢家订单
            Order winner = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                    .eq(Order::getRequestId, dto.getRequestId()));
            if (winner != null) {
                log.info("幂等命中（唯一索引兜底）: requestId={}, orderNo={}", dto.getRequestId(), winner.getOrderNo());
                return toVO(winner, orderItemMapper.selectByOrderId(winner.getId()));
            }
            // 理论上到不了这里：冲突却查不到赢家，说明数据异常
            throw new BusinessException(ErrorCode.DUPLICATE_REQUEST);
        }
    }

    /**
     * 下单主流程（事务方法）：限购校验 → 逐商品校验/原子扣库存 → 落主单与快照明细。
     * <p>任何一步抛异常，整个事务回滚——包括前面已扣的库存，保证「要么全成，要么全没发生」。
     * 唯一索引冲突（DuplicateKeyException）不在此 catch，让它滚出事务触发回滚，
     * 由外层 placeOrder 统一处理。</p>
     *
     * @param dto 下单入参
     * @return 新订单 VO
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderVO doPlaceOrder(OrderCreateDTO dto) {
        // 限购校验（定稿 v2）：跨字段规则（累加和）归 Service，校验注解做不了
        int totalQuantity = dto.getItems().stream()
                .mapToInt(OrderCreateDTO.Item::getQuantity).sum();
        if (totalQuantity > 99) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "单个订单总件数不能超过99");
        }

        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderCreateDTO.Item i : dto.getItems()) {
            Product product = productMapper.selectById(i.getProductId());   // @TableLogic 自动过滤已删除
            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "商品不存在: " + i.getProductId());
            }
            if (product.getStatus() != 1) {
                throw new BusinessException(ErrorCode.PRODUCT_OFF_SHELF, "商品已下架: " + product.getName());
            }
            // 原子扣库存：stock >= n 在 SQL 的 WHERE 里，数据库行锁保证不超卖；
            // 返回 0 行 = 库存不足，抛异常让整个事务回滚（连带还原本轮已扣的其他商品）
            if (productMapper.deductStock(product.getId(), i.getQuantity()) == 0) {
                throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH, "库存不足: " + product.getName());
            }
            // 明细落「快照」：名称/图片/单价拷贝自下单时刻，商品以后改名改价不影响本单
            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getMainImage());
            item.setPrice(product.getPrice());
            item.setQuantity(i.getQuantity());
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(i.getQuantity()));
            item.setSubtotal(subtotal);
            items.add(item);
            totalAmount = totalAmount.add(subtotal);
        }

        // 写主单：uk_request_id 是幂等的最后防线，并发赢家在这里决出
        Order order = new Order();
        order.setOrderNo(OrderNoUtil.generate());
        order.setUserId(dto.getUserId());
        order.setTotalAmount(totalAmount);
        order.setTotalQuantity(totalQuantity);
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setRemark(dto.getRemark());
        order.setRequestId(dto.getRequestId());
        orderMapper.insert(order);   // insert 后 MP 回填自增 id；冲突则 DuplicateKeyException 滚出事务

        // 写明细：orderId/orderNo 依赖主单 insert 后的回填，必须在 insert 之后
        items.forEach(item -> {
            item.setOrderId(order.getId());
            item.setOrderNo(order.getOrderNo());
            orderItemMapper.insert(item);
        });

        log.info("下单成功: orderNo={}, userId={}, totalAmount={}, items={}",
                order.getOrderNo(), order.getUserId(), order.getTotalAmount(), items.size());
        return toVO(order, items);
    }

    /**
     * 模拟支付：待支付(10) → 已支付(20)。
     * <p>条件更新 WHERE status=10：两个并发「支付」只成功一次，失败方拿 0 行 → 状态非法。</p>
     *
     * @param orderNo 订单号
     * @return 支付后的订单 VO
     */
    @Override
    public OrderVO pay(String orderNo) {
        Order order = requireByOrderNo(orderNo);
        if (orderMapper.updateStatusToPaid(order.getId()) == 0) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID,
                    "当前状态不能支付: " + statusDesc(order));
        }
        log.info("支付成功: orderNo={}, amount={}", orderNo, order.getTotalAmount());
        return reloadVO(order.getId());
    }

    /**
     * 取消订单（定稿 v2）：待支付(10)普通取消；已支付(20)取消并模拟退款；其余状态拒绝。
     * <p>条件更新 WHERE status=期望值 兜住并发（支付与取消同时发生只有一方成功）；
     * 成功后<b>同一事务</b>逐明细回补库存——只改状态不回补=库存泄漏，只回补不改状态=订单还能被支付。</p>
     *
     * @param orderNo 订单号
     * @return 取消后的订单 VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO cancel(String orderNo) {
        Order order = requireByOrderNo(orderNo);
        boolean fromPaid = order.getStatus() == OrderStatus.PAID.getCode();
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT.getCode() && !fromPaid) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID,
                    "当前状态不能取消: " + statusDesc(order));
        }
        // 条件更新：期望状态入参化，10→50 与 20→50 共用这一个方法
        if (orderMapper.updateStatusToCancelled(order.getId(), order.getStatus()) == 0) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "订单状态已被并发修改，取消失败");
        }
        // 回补库存必须与状态变更同一事务
        List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
        items.forEach(item -> productMapper.restoreStock(item.getProductId(), item.getQuantity()));
        if (fromPaid) {
            log.info("模拟退款: orderNo={}, 金额={} 元已原路退回（模拟，无真实资金流）",
                    orderNo, order.getTotalAmount());
        }
        log.info("取消订单: orderNo={}, 回补库存 {} 项", orderNo, items.size());
        return reloadVO(order.getId());
    }

    /**
     * 订单详情（主单 + 明细）。
     *
     * @param orderNo 订单号
     * @return 订单 VO
     */
    @Override
    public OrderVO getDetail(String orderNo) {
        Order order = requireByOrderNo(orderNo);
        return toVO(order, orderItemMapper.selectByOrderId(order.getId()));
    }

    /**
     * 订单分页查询：可按 userId / status 过滤，按创建时间倒序。列表不带明细（详情接口才查）。
     *
     * @param query 分页与过滤条件
     * @return 分页结果
     */
    @Override
    public PageResult<OrderVO> page(OrderQueryDTO query) {
        Page<Order> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(query.getUserId() != null, Order::getUserId, query.getUserId())
                .eq(query.getStatus() != null, Order::getStatus, query.getStatus())
                .orderByDesc(Order::getCreateTime);
        return PageResult.of(orderMapper.selectPage(page, wrapper), this::toListVO);
    }

    /**
     * 按订单号查询订单，不存在抛 ORDER_NOT_FOUND。
     *
     * @param orderNo 订单号
     * @return 订单实体
     */
    private Order requireByOrderNo(String orderNo) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在: " + orderNo);
        }
        return order;
    }

    /**
     * 重查订单并组装 VO（状态流转后取最新数据返回）。
     *
     * @param orderId 订单 id
     * @return 订单 VO
     */
    private OrderVO reloadVO(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        return toVO(order, orderItemMapper.selectByOrderId(orderId));
    }

    /**
     * 状态中文描述（异常信息用），未知状态给兜底文案。
     *
     * @param order 订单实体
     * @return 状态中文
     */
    private String statusDesc(Order order) {
        OrderStatus status = OrderStatus.of(order.getStatus());
        return status == null ? "未知(" + order.getStatus() + ")" : status.getDesc();
    }

    /**
     * 实体转 VO（主单 + 明细），状态码翻译为中文描述。
     *
     * @param order 订单实体
     * @param items 明细实体列表
     * @return 订单 VO
     */
    private OrderVO toVO(Order order, List<OrderItem> items) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setTotalQuantity(order.getTotalQuantity());
        vo.setStatus(order.getStatus());
        OrderStatus status = OrderStatus.of(order.getStatus());
        vo.setStatusDesc(status == null ? "未知状态" : status.getDesc());
        vo.setRemark(order.getRemark());
        vo.setCreateTime(order.getCreateTime());
        vo.setItems(items.stream().map(this::toItemVO).toList());
        return vo;
    }

    /**
     * 实体转列表 VO（不带明细——分页列表 N 单 × 查明细 = N+1 次查询，明细留给详情接口）。
     *
     * @param order 订单实体
     * @return 订单 VO（items 为 null）
     */
    private OrderVO toListVO(Order order) {
        OrderVO vo = toVO(order, List.of());
        vo.setItems(null);
        return vo;
    }

    /**
     * 明细实体转 VO。
     *
     * @param item 明细实体
     * @return 明细 VO
     */
    private OrderItemVO toItemVO(OrderItem item) {
        OrderItemVO vo = new OrderItemVO();
        vo.setId(item.getId());
        vo.setProductId(item.getProductId());
        vo.setProductName(item.getProductName());
        vo.setProductImage(item.getProductImage());
        vo.setPrice(item.getPrice());
        vo.setQuantity(item.getQuantity());
        vo.setSubtotal(item.getSubtotal());
        return vo;
    }
}
