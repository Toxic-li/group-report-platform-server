package com.groupreport.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 条件格式VO
 */
@Data
@Schema(description = "条件格式信息")
public class ConditionalFormatVO {

    @Schema(description = "规则ID")
    private Long id;

    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "规则名称")
    private String name;

    @Schema(description = "条件类型：cell_value/formula/top_bottom/duplicate/blank")
    private String conditionType;

    @Schema(description = "运算符")
    private String operator;

    @Schema(description = "条件值1")
    private Object value;

    @Schema(description = "条件值2")
    private Object value2;

    @Schema(description = "应用范围")
    private String applyRange;

    @Schema(description = "样式配置")
    private Map<String, Object> style;

    @Schema(description = "停止处理后续规则")
    private Boolean stopIfTrue;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
