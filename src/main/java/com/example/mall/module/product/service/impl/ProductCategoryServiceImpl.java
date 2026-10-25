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

    /**
     * 新增商品分类。
     * <p>sort 不传时默认为 0（DTO 里已给默认值）；新增成功的 id 由 MP 回填到实体后返回。</p>
     *
     * @param dto 分类入参（name 必填、最长 32 字）
     * @return 新增分类的主键 id
     */
    @Override
    public Long create(CategorySaveDTO dto) {
        ProductCategory category = new ProductCategory();
        category.setName(dto.getName());
        category.setSort(dto.getSort());
        categoryMapper.insert(category);   // insert 后 MP 自动回填自增 id
        log.info("新增分类: id={}, name={}", category.getId(), category.getName());
        return category.getId();
    }

    /**
     * 修改分类名称/排序。
     * <p>只 set 非空字段（updateById 忽略 null），不会误伤 status、create_time 等其他列。</p>
     *
     * @param dto 入参，id 必填且必须存在，否则抛 NOT_FOUND
     */
    @Override
    public void update(CategorySaveDTO dto) {
        requireExists(dto.getId());
        ProductCategory category = new ProductCategory();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setSort(dto.getSort());
        categoryMapper.updateById(category);   // 只更新非 null 字段，不会误伤 status
        log.info("修改分类: id={}, name={}", dto.getId(), dto.getName());
    }

    /**
     * 删除分类（逻辑删除）。
     * <p>deleteById 因实体上有 @TableLogic，实际执行 UPDATE ... SET deleted=1，行仍在库里。</p>
     *
     * @param id 分类 id，必须存在
     */
    @Override
    public void delete(Long id) {
        requireExists(id);
        categoryMapper.deleteById(id);         // @TableLogic 生效：实际执行 UPDATE deleted=1
        log.info("删除分类(逻辑删除): id={}", id);
    }

    /**
     * 查询全量分类列表，按 sort 升序返回。
     * <p>分类数量少不做分页；@TableLogic 生效，MP 自动追加 WHERE deleted=0，
     * 已删除的分类对调用方不可见。</p>
     *
     * @return 未删除的分类列表（已转 VO）
     */
    @Override
    public List<CategoryVO> list() {
        // @TableLogic 生效：MP 自动追加 WHERE deleted=0
        return categoryMapper.selectList(new LambdaQueryWrapper<ProductCategory>()
                        .orderByAsc(ProductCategory::getSort))
                .stream().map(this::toVO).toList();
    }

    /**
     * 校验分类存在，不存在（或 id 为空）时抛业务异常。
     *
     * @param id 分类 id
     */
    private void requireExists(Long id) {
        if (id == null || categoryMapper.selectById(id) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "分类不存在");
        }
    }

    /**
     * 实体转 VO（出参对象，屏蔽持久层细节）。
     *
     * @param category 分类实体
     * @return 分类 VO
     */
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
