package com.groupreport.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 校验规则VO
 */
@Data
@Schema(description = "校验规则信息")
public class ValidatorVO {

    @Schema(description = "规则ID")
    private Long id;

    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "规则名称")
    private String name;

    @Schema(description = "校验类型：not_null/range/regex/custom/business")
    private String type;

    @Schema(description = "目标行编码列表")
    private List<String> targetRows;

    @Schema(description = "目标列编码列表")
    private List<String> targetColumns;

    @Schema(description = "规则配置")
    private Map<String, Object> ruleConfig;

    @Schema(description = "错误提示信息")
    private String errorMessage;

    @Schema(description = "校验时机：input/save/submit")
    private String validateTrigger;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
