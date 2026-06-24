package com.groupreport.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公式响应VO — 对齐前端 Univer 格式
 */
@Data
@Schema(description = "公式信息")
public class FormulaVO {

    /** ========== 基础字段 ========== */

    @Schema(description = "公式ID")
    private Long id;

    @Schema(description = "模板ID")
    private String templateId;

    @Schema(description = "模板名称")
    private String templateName;

    /** ========== 核心字段（与前端一致）========== */

    @Schema(description = "字段标识，如：completionRate")
    private String fieldName;

    @Schema(description = "显示标签，如：完成率")
    private String label;

    @Schema(description = "公式表达式")
    private String expression;

    @Schema(description = "结果类型：number-数值 string-文本 percent-百分比 currency-金额")
    private String resultType;

    @Schema(description = "目标单元格位置，如：3-5")
    private String targetCell;

    @Schema(description = "依赖字段列表")
    private List<String> dependencies;

    /** ========== 后端扩展字段（辅助信息）========== */

    @Schema(description = "目标行编码")
    private String targetRowCode;

    @Schema(description = "目标列编码")
    private String targetColumnCode;

    @Schema(description = "源数据范围（自动从表达式解析或手动指定）")
    private String sourceRange;

    @Schema(description = "计算触发方式")
    private String calcTrigger;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    /** ========== 时间戳 ========== */

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
