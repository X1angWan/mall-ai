package com.example.mall.controller;

import com.example.mall.common.BusinessException;
import com.example.mall.common.ErrorCode;
import com.example.mall.common.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 临时验收接口（04 章验证通用底座用，09 章删除）
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    /** 验证统一返回体 */
    @GetMapping("/ok")
    public Result<String> ok() {
        return Result.ok("hello");
    }

    /** 验证业务异常收敛 */
    @GetMapping("/biz")
    public Result<Void> biz() {
        throw new BusinessException(ErrorCode.BUSINESS_ERROR, "演示业务异常");
    }

    /** 验证参数校验异常收敛（jakarta.validation，不是 javax） */
    @PostMapping("/valid")
    public Result<String> valid(@Valid @RequestBody DemoValidDTO dto) {
        return Result.ok(dto.getName());
    }

    @Data
    public static class DemoValidDTO {

        @NotBlank(message = "name不能为空")
        private String name;
    }
}
