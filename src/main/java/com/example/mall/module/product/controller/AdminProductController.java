package com.example.mall.module.product.controller;

import com.example.mall.common.Result;
import com.example.mall.module.product.dto.ProductSaveDTO;
import com.example.mall.module.product.dto.StockAdjustDTO;
import com.example.mall.module.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品管理接口（08 章加 @SaCheckRole("ADMIN")）
 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @PostMapping
    public Result<Long> create(@Valid @RequestBody ProductSaveDTO dto) {
        return Result.ok(productService.create(dto));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody ProductSaveDTO dto) {
        productService.update(dto);
        return Result.ok();
    }

    @PutMapping("/{id}/on-shelf")
    public Result<Void> onShelf(@PathVariable Long id) {
        productService.onShelf(id);
        return Result.ok();
    }

    @PutMapping("/{id}/off-shelf")
    public Result<Void> offShelf(@PathVariable Long id) {
        productService.offShelf(id);
        return Result.ok();
    }

    @PutMapping("/{id}/stock")
    public Result<Void> adjustStock(@PathVariable Long id, @Valid @RequestBody StockAdjustDTO dto) {
        productService.adjustStock(id, dto.getDelta());
        return Result.ok();
    }
}
