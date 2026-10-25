package com.example.mall.module.product.controller;

import com.example.mall.common.PageResult;
import com.example.mall.common.Result;
import com.example.mall.module.product.dto.ProductQueryDTO;
import com.example.mall.module.product.dto.ProductVO;
import com.example.mall.module.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品浏览接口（用户端，无需登录）
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class UserProductController {

    private final ProductService productService;

    /** 分页查询上架商品：GET /api/products?pageNum=1&pageSize=10&name=AI&categoryId=1 */
    @GetMapping
    public Result<PageResult<ProductVO>> page(ProductQueryDTO query) {
        return Result.ok(productService.page(query));
    }

    @GetMapping("/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.ok(productService.getDetail(id));
    }
}
