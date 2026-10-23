package com.example.mall.module.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 分类新增/修改入参（id 为空=新增，id 非空=修改，修改时 id 不存在由 Service 校验）
 */
@Data
public class CategorySaveDTO {

    /** 修改时必填（新增时不传） */
    private Long id;

    @NotBlank(message = "name不能为空")
    @Size(max = 32, message = "name最长 32 字")
    private String name;

    /** 排序值，默认 0 */
    private Integer sort = 0;
}
