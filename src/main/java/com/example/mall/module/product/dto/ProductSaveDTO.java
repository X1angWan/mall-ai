package com.example.mall.module.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品新增/修改入参（id 为空=新增，非空=修改）
 */
@Data
public class ProductSaveDTO {

    /** 修改时必填（新增时不传） */
    private Long id;

    @NotNull(message = "categoryId不能为空")
    private Long categoryId;

    @NotBlank(message = "name不能为空")
    @Size(max = 100, message = "name最长100字")
    private String name;

    @Size(max = 200, message = "subtitle最长200字")
    private String subtitle;

    private String mainImage;

    @NotNull(message = "price不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "price必须大于0")
    @Digits(integer = 10, fraction = 2, message = "price最多两位小数")
    private BigDecimal price;

    @NotNull(message = "stock不能为空")
    @Min(value = 0, message = "stock不能为负数")
    private Integer stock;
}
