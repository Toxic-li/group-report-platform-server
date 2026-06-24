package com.groupreport.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 条件格式DTO
 */
@Data
@Schema(description = "条件格式请求")
public class ConditionalFormatDTO {

    @Schema(description = "规则ID（更新时必填）")
    private Long id;

    @NotNull(message = "模板ID不能为空")
    private Long templateId;

    @NotBlank(message = "规则名称不能为空")
    @Schema(description = "规则名称")
    private String name;

    @NotNull(message = "条件类型不能为空")
    @Schema(description = "条件类型：cell_value/formula/top_bottom/duplicate/blank")
    private String conditionType;

    @Schema(description = "运算符：eq/ne/gt/ge/lt/le/between/contains")
    private String operator;

    @Schema(description = "条件值1")
    private Object value;

    @Schema(description = "条件值2（between时使用）")
    private Object value2;

    @NotBlank(message = "应用范围不能为空")
    @Schema(description = "应用范围，如：R1C1:R10C5")
    private String applyRange;

    @Schema(description = "样式配置")
    private Map<String, Object> style;

    @Schema(description = "停止处理后续规则")
    private Boolean stopIfTrue;

    @Schema(description = "排序号")
    private Integer sortOrder = 0;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status = 1;
}
