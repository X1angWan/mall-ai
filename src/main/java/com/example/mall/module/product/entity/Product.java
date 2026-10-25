package com.example.mall.module.product.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体
 */
@Data
@TableName("t_product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类 id */
    private Long categoryId;

    /** 商品名称 */
    private String name;

    /** 副标题 */
    private String subtitle;

    /** 主图 URL */
    private String mainImage;

    /** 售价：金额一律 BigDecimal，禁止 double/float */
    private BigDecimal price;

    /** 库存 */
    private Integer stock;

    /** 状态：1 上架 0 下架 */
    private Integer status;

    /** 乐观锁版本号：配合 OptimisticLockerInnerInterceptor */
    @Version
    private Integer version;

    /** 逻辑删除：MP 查询/删除自动拼 deleted=0 */
    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
