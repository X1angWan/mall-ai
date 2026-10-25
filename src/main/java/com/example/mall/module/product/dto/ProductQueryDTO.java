package com.example.mall.module.product.dto;

import lombok.Data;

/**
 * 商品分页查询入参
 */
@Data
public class ProductQueryDTO {

    private long pageNum = 1;
    private long pageSize = 10;

    /** 商品名模糊搜索 */
    private String name;

    /** 分类过滤 */
    private Long categoryId;
}
