package com.groupreport.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 校验规则DTO — 对齐设计器JSON格式
 */
@Data
@Schema(description = "校验规则请求")
public class ValidatorDTO {

    @Schema(description = "规则ID（更新时必填）")
    private Long id;

    @NotNull(message = "模板ID不能为空")
    @Schema(description = "模板ID", required = true)
    private Long templateId;

    @NotBlank(message = "规则名称不能为空")
    @Schema(description = "规则名称，如：金额非空校验", required = true)
    private String name;

    @NotBlank(message = "校验类型不能为空")
    @Schema(description = "校验类型：not_null/range/regex/custom/business", required = true)
    private String type;

    @Schema(description = "目标行编码列表")
    private List<String> targetRows;

    @Schema(description = "目标列编码列表")
    private List<String> targetColumns;

    @Schema(description = "规则配置（根据type不同含义不同）")
    private Map<String, Object> ruleConfig;

    @NotBlank(message = "错误提示不能为空")
    @Schema(description = "错误提示信息", required = true)
    private String errorMessage;

    @Schema(description = "校验时机：input/save/submit")
    private String validateTrigger = "save";

    @Schema(description = "优先级")
    private Integer priority = 0;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status = 1;
}
