package com.example.mall.module.product.service;

import com.example.mall.module.product.dto.CategorySaveDTO;
import com.example.mall.module.product.dto.CategoryVO;

import java.util.List;

/**
 * 商品分类 Service
 */
public interface ProductCategoryService {

    /** 新增分类，返回 id */
    Long create(CategorySaveDTO dto);

    /** 修改分类 */
    void update(CategorySaveDTO dto);

    /** 删除分类（逻辑删除） */
    void delete(Long id);

    /** 全量列表，按 sort 升序（分类数量少，不分页） */
    List<CategoryVO> list();
}
