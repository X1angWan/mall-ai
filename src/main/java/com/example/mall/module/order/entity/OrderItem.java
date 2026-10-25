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
 * 订单明细实体：下单时刻的商品快照。
 * <p>名称/图片/价格拷贝自下单时的商品数据——商品以后改名改价不影响历史订单展示与对账。</p>
 */
@Data
@TableName("t_order_item")
public class OrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属订单 id（t_order.id） */
    private Long orderId;

    /** 订单号（冗余字段，便于按订单号查明细） */
    private String orderNo;

    /** 商品 id（快照来源） */
    private Long productId;

    /** 商品名称快照 */
    private String productName;

    /** 商品主图快照 */
    private String productImage;

    /** 成交单价快照（下单时刻的售价） */
    private BigDecimal price;

    /** 购买数量 */
    private Integer quantity;

    /** 小计 = price × quantity */
    private BigDecimal subtotal;

    /** 逻辑删除：MP 查询/删除自动拼 deleted=0 */
    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
