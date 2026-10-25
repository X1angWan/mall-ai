package com.example.mall.module.product.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 库存调整入参：delta 正数补货、负数核减，调整后不得为负
 */
@Data
public class StockAdjustDTO {

    @NotNull(message = "delta不能为空")
    private Integer delta;
}
