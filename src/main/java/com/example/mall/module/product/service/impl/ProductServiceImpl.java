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

    /**
     * 新增商品，默认上架（status=1）。
     * <p>create_time/update_time 由 MetaObjectHandler 自动填充，无需手动赋值。</p>
     *
     * @param dto 商品入参（校验规则见 ProductSaveDTO 上的注解）
     * @return 新增商品的主键 id
     */
    @Override
    public Long create(ProductSaveDTO dto) {
        Product product = new Product();
        copySaveFields(dto, product);
        product.setStatus(1);
        productMapper.insert(product);   // insert 后 MP 自动回填自增 id
        log.info("新增商品: id={}, name={}, price={}, stock={}",
                product.getId(), product.getName(), product.getPrice(), product.getStock());
        return product.getId();
    }

    /**
     * 修改商品基本信息（名称/副标题/主图/价格/库存/分类）。
     * <p>关键点：新 new 的实体 version 为 null，updateById 生成 SET 子句时忽略 null 字段，
     * 因此不会覆盖乐观锁版本号，也不会碰 status——已上架状态不受编辑影响。</p>
     *
     * @param dto 入参，id 必填且必须存在
     */
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

    /**
     * 商品上架（status 0 → 1）。
     *
     * @param id 商品 id
     */
    @Override
    public void onShelf(Long id) {
        changeShelf(id, 1);
    }

    /**
     * 商品下架（status 1 → 0），下架后用户端不可见、不可下单。
     *
     * @param id 商品 id
     */
    @Override
    public void offShelf(Long id) {
        changeShelf(id, 0);
    }

    /**
     * 上下架公共实现，当前状态与目标状态相同时拒绝（幂等地「翻状态」没意义，提示调用方）。
     * <p>关键点：用 LambdaUpdateWrapper 只更新 status 一个字段——
     * 若用 updateById 全量更新，会把并发场景下别人刚扣减的 stock 用旧值覆盖掉。</p>
     *
     * @param id           商品 id
     * @param targetStatus 目标状态：1 上架 0 下架
     */
    private void changeShelf(Long id, int targetStatus) {
        Product existing = requireExists(id);
        if (existing.getStatus() == targetStatus) {
            throw new BusinessException(ErrorCode.PARAM_ERROR,
                    targetStatus == 1 ? "商品已是上架状态" : "商品已是下架状态");
        }
        // 只更新 status 一个字段，避免覆盖并发中的 stock 变更
        productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, id)
                .set(Product::getStatus, targetStatus));
        log.info("商品{}: id={}", targetStatus == 1 ? "上架" : "下架", id);
    }

    /**
     * 库存调整：delta 正数补货、负数核减。
     * <p>关键点：stock + delta >= 0 放进 WHERE 条件，配合 SET stock = stock + delta
     * 组成单条原子 UPDATE——负库存被数据库直接拒绝，不依赖「先查后改」（那有并发漏洞）。
     * 返回影响行数为 0 即说明调整后会是负数（存在性已提前校验过）。</p>
     *
     * @param id    商品 id
     * @param delta 调整增量，非 0 整数
     */
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

    /**
     * 用户端分页查询，仅返回上架商品（status=1）。
     * <p>支持 name 模糊（like）与 categoryId 精确过滤，按创建时间倒序（新品在前）。</p>
     *
     * @param query 分页与过滤条件（页码/页大小有默认值）
     * @return 分页结果（实体已转 VO）
     */
    @Override
    public PageResult<ProductVO> page(ProductQueryDTO query) {
        Page<Product> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)                                              // 仅上架
                .like(StrUtil.isNotBlank(query.getName()), Product::getName, query.getName())
                .eq(query.getCategoryId() != null, Product::getCategoryId, query.getCategoryId())
                .orderByDesc(Product::getCreateTime);
        // selectPage 由分页插件自动改写为「查总数 + LIMIT」两条 SQL
        return PageResult.of(productMapper.selectPage(page, wrapper), this::toVO);
    }

    /**
     * 商品详情（用户端视角）。
     * <p>已删除（@TableLogic 自动过滤）→ PRODUCT_NOT_FOUND；存在但已下架 → PRODUCT_OFF_SHELF。
     * 两种情况分开给码，前端能区分展示「不存在」和「已下架」。</p>
     *
     * @param id 商品 id
     * @return 商品 VO
     */
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

    /**
     * 校验商品存在并返回实体，不存在（或 id 为空）时抛 PRODUCT_NOT_FOUND。
     *
     * @param id 商品 id
     * @return 商品实体
     */
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

    /**
     * 把保存入参的字段拷到实体（新增/修改复用）。
     *
     * @param dto     保存入参
     * @param product 目标实体
     */
    private void copySaveFields(ProductSaveDTO dto, Product product) {
        product.setCategoryId(dto.getCategoryId());
        product.setName(dto.getName());
        product.setSubtitle(dto.getSubtitle());
        product.setMainImage(dto.getMainImage());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
    }

    /**
     * 实体转 VO（不暴露 version/deleted 等持久层字段）。
     *
     * @param product 商品实体
     * @return 商品 VO
     */
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
