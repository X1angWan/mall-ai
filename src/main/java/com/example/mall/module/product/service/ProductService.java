package com.example.mall.module.product.service;

import com.example.mall.common.PageResult;
import com.example.mall.module.product.dto.ProductQueryDTO;
import com.example.mall.module.product.dto.ProductSaveDTO;
import com.example.mall.module.product.dto.ProductVO;

/**
 * 商品 Service
 */
public interface ProductService {

    /** 新增商品（默认上架），返回 id */
    Long create(ProductSaveDTO dto);

    /** 修改商品基本信息（不碰 version/status/stock） */
    void update(ProductSaveDTO dto);

    /** 上架 */
    void onShelf(Long id);

    /** 下架 */
    void offShelf(Long id);

    /** 库存调整：delta 正数补货、负数核减，调整后不得为负 */
    void adjustStock(Long id, int delta);

    /** 用户端分页查询（仅上架商品） */
    PageResult<ProductVO> page(ProductQueryDTO query);

    /** 商品详情（未找到/已下架抛业务异常） */
    ProductVO getDetail(Long id);
}
