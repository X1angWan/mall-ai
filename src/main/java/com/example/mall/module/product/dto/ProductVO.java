package com.example.mall.module.product.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品出参：不暴露 version / deleted
 */
@Data
public class ProductVO {

    private Long id;
    private Long categoryId;
    private String name;
    private String subtitle;
    private String mainImage;
    private BigDecimal price;
    private Integer stock;
    private Integer status;
    private LocalDateTime createTime;
}
