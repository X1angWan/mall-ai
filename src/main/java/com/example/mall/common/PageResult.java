package com.example.mall.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

/**
 * 分页返回对象
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> list;
    private long total;
    private long pageNum;
    private long pageSize;

    /**
     * MP 分页结果转换：实体到 VO 的映射由调用方以函数式参数提供
     *
     * @param page      MP 分页查询结果
     * @param converter 实体转 VO 的映射函数（如 this::toVO）
     */
    public static <E, V> PageResult<V> of(IPage<E> page, Function<E, V> converter) {
        PageResult<V> result = new PageResult<>();
        result.setList(page.getRecords().stream().map(converter).toList());
        result.setTotal(page.getTotal());
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        return result;
    }
}
