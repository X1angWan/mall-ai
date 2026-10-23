package com.example.mall.module.product.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类出参
 */
@Data
public class CategoryVO {

    private Long id;
    private String name;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
}
