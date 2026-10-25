package com.example.mall.module.order.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主表实体。
 * <p>注意：订单不用 @Version 乐观锁——并发控制全部靠「条件更新」（WHERE status=当前值），
 * 因为订单的并发冲突点是状态流转，不是整行数据。</p>
 */
@Data
@TableName("t_order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号：ORD + 时间戳 + 随机数，对外展示用 */
    private String orderNo;

    /** 下单用户 id（演示阶段由入参传入，08 章改为登录态获取） */
    private Long userId;

    /** 订单总金额 = 各明细小计之和 */
    private BigDecimal totalAmount;

    /** 商品总件数 */
    private Integer totalQuantity;

    /** 状态：见 OrderStatus 枚举 */
    private Integer status;

    /** 订单备注 */
    private String remark;

    /** 下单幂等号（客户端生成 UUID），uk_request_id 唯一索引是幂等最后防线 */
    private String requestId;

    /** 逻辑删除：MP 查询/删除自动拼 deleted=0 */
    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
