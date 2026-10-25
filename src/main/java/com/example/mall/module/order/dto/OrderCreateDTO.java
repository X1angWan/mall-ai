package com.example.mall.module.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 下单入参。
 * <p>幂等约定：前端每次「下单意图」生成一个新 UUID；同一次意图的重试（超时重发）复用同一个 requestId，
 * 服务端按 requestId 去重，重复提交只会产生一个订单。</p>
 */
@Data
public class OrderCreateDTO {

    /** 下单用户 id（演示阶段由前端传入，08 章改为登录态获取） */
    @NotNull(message = "userId不能为空")
    private Long userId;

    /** 下单幂等号（客户端生成 UUID） */
    @NotBlank(message = "requestId不能为空")
    @Size(max = 64, message = "requestId最长64字符")
    private String requestId;

    /** 购买清单；@Valid 级联校验元素内部注解 */
    @NotEmpty(message = "items不能为空")
    @Size(max = 20, message = "单订单商品种类不能超过20")
    @Valid
    private List<Item> items;

    /** 订单备注，可空 */
    private String remark;

    /**
     * 单个购买项
     */
    @Data
    public static class Item {

        @NotNull(message = "productId不能为空")
        private Long productId;

        @NotNull(message = "quantity不能为空")
        @Min(value = 1, message = "quantity至少为1")
        private Integer quantity;
    }
}
