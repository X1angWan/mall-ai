package com.example.mall.module.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.BusinessException;
import com.example.mall.common.ErrorCode;
import com.example.mall.module.product.dto.CategorySaveDTO;
import com.example.mall.module.product.dto.CategoryVO;
import com.example.mall.module.product.entity.ProductCategory;
import com.example.mall.module.product.mapper.ProductCategoryMapper;
import com.example.mall.module.product.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品分类 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryMapper categoryMapper;

    @Override
    public Long create(CategorySaveDTO dto) {
        ProductCategory category = new ProductCategory();
        category.setName(dto.getName());
        category.setSort(dto.getSort());
        categoryMapper.insert(category);
        log.info("新增分类: id={}, name={}", category.getId(), category.getName());
        return category.getId();
    }

    @Override
    public void update(CategorySaveDTO dto) {
        requireExists(dto.getId());
        ProductCategory category = new ProductCategory();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setSort(dto.getSort());
        categoryMapper.updateById(category);   // 只更新非 null 字段，不会误伤 status
    }

    @Override
    public void delete(Long id) {
        requireExists(id);
        categoryMapper.deleteById(id);         // @TableLogic 生效：实际执行 UPDATE deleted=1
        log.info("删除分类(逻辑删除): id={}", id);
    }

    @Override
    public List<CategoryVO> list() {
        // @TableLogic 生效：MP 自动追加 WHERE deleted=0
        return categoryMapper.selectList(new LambdaQueryWrapper<ProductCategory>()
                        .orderByAsc(ProductCategory::getSort))
                .stream().map(this::toVO).toList();
    }

    private void requireExists(Long id) {
        if (id == null || categoryMapper.selectById(id) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "分类不存在");
        }
    }

    private CategoryVO toVO(ProductCategory category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setSort(category.getSort());
        vo.setStatus(category.getStatus());
        vo.setCreateTime(category.getCreateTime());
        return vo;
    }
}
