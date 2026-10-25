package com.example.mall.module.product.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mall.common.BusinessException;
import com.example.mall.common.ErrorCode;
import com.example.mall.common.PageResult;
import com.example.mall.module.product.dto.ProductQueryDTO;
import com.example.mall.module.product.dto.ProductSaveDTO;
import com.example.mall.module.product.dto.ProductVO;
import com.example.mall.module.product.entity.Product;
import com.example.mall.module.product.mapper.ProductMapper;
import com.example.mall.module.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 商品 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public Long create(ProductSaveDTO dto) {
        Product product = new Product();
        copySaveFields(dto, product);
        product.setStatus(1);
        productMapper.insert(product);
        log.info("新增商品: id={}, name={}, price={}, stock={}",
                product.getId(), product.getName(), product.getPrice(), product.getStock());
        return product.getId();
    }

    @Override
    public void update(ProductSaveDTO dto) {
        requireExists(dto.getId());
        // 只 set 非空字段：version 为 null 不会进 SET，status/stock 不受影响
        Product product = new Product();
        product.setId(dto.getId());
        copySaveFields(dto, product);
        productMapper.updateById(product);
        log.info("修改商品: id={}, name={}", product.getId(), product.getName());
    }

    @Override
    public void onShelf(Long id) {
        changeShelf(id, 1);
    }

    @Override
    public void offShelf(Long id) {
        changeShelf(id, 0);
    }

    /** 上下架状态判断在 Service：只更新 status 一个字段，避免覆盖并发中的 stock 变更 */
    private void changeShelf(Long id, int targetStatus) {
        Product existing = requireExists(id);
        if (existing.getStatus() == targetStatus) {
            throw new BusinessException(ErrorCode.PARAM_ERROR,
                    targetStatus == 1 ? "商品已是上架状态" : "商品已是下架状态");
        }
        productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, id)
                .set(Product::getStatus, targetStatus));
        log.info("商品{}: id={}", targetStatus == 1 ? "上架" : "下架", id);
    }

    @Override
    public void adjustStock(Long id, int delta) {
        requireExists(id);
        if (delta == 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "调整值delta不能为0");
        }
        // 原子更新：stock + delta >= 0 放进 WHERE，负数结果直接被数据库拒绝
        int rows = productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, id)
                .apply("stock + {0} >= 0", delta)
                .setSql("stock = stock + {0}", delta));
        if (rows == 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "库存调整后不能为负数");
        }
        log.info("调整库存: id={}, delta={}", id, delta);
    }

    @Override
    public PageResult<ProductVO> page(ProductQueryDTO query) {
        Page<Product> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)                                              // 仅上架
                .like(StrUtil.isNotBlank(query.getName()), Product::getName, query.getName())
                .eq(query.getCategoryId() != null, Product::getCategoryId, query.getCategoryId())
                .orderByDesc(Product::getCreateTime);
        return PageResult.of(productMapper.selectPage(page, wrapper), this::toVO);
    }

    @Override
    public ProductVO getDetail(Long id) {
        Product product = productMapper.selectById(id);   // @TableLogic 自动过滤已删除
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if (product.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PRODUCT_OFF_SHELF);
        }
        return toVO(product);
    }

    private Product requireExists(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "商品id不能为空");
        }
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    private void copySaveFields(ProductSaveDTO dto, Product product) {
        product.setCategoryId(dto.getCategoryId());
        product.setName(dto.getName());
        product.setSubtitle(dto.getSubtitle());
        product.setMainImage(dto.getMainImage());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
    }

    private ProductVO toVO(Product product) {
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setCategoryId(product.getCategoryId());
        vo.setName(product.getName());
        vo.setSubtitle(product.getSubtitle());
        vo.setMainImage(product.getMainImage());
        vo.setPrice(product.getPrice());
        vo.setStock(product.getStock());
        vo.setStatus(product.getStatus());
        vo.setCreateTime(product.getCreateTime());
        return vo;
    }
}
