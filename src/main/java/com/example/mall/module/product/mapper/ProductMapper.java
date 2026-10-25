package com.example.mall.module.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.module.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 商品 Mapper
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 原子扣减库存：stock >= n 放进 WHERE，由数据库行锁保证不超卖。
     * 返回影响行数，0 表示库存不足（或商品不存在）。
     * ⚠ 禁止改成「先查库存再减」的两步写法，高并发下必超卖。
     */
    @Update("UPDATE t_product SET stock = stock - #{quantity} " +
            "WHERE id = #{productId} AND stock >= #{quantity} AND deleted = 0")
    int deductStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 回补库存（取消订单/关单时使用），返回影响行数。
     */
    @Update("UPDATE t_product SET stock = stock + #{quantity} " +
            "WHERE id = #{productId} AND deleted = 0")
    int restoreStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
