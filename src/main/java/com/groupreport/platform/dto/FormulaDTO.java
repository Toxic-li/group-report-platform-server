package com.groupreport.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 公式保存/更新DTO — 对齐前端 Univer 传输格式
 */
@Data
@Schema(description = "公式请求")
public class FormulaDTO {

    @Schema(description = "公式ID（更新时必填）")
    private Long id;

    /** ========== 核心字段（与前端一致）========== */

    @NotBlank(message = "字段标识不能为空")
    @Schema(description = "字段标识，如：completionRate", required = true)
    private String fieldName;

    @NotBlank(message = "显示名称不能为空")
    @Schema(description = "显示标签，如：完成率", required = true)
    private String label;

    @NotBlank(message = "公式表达式不能为空")
    @Schema(description = "公式表达式，如：SUM(r_raw, m_raw_coal) 或 R1C1+R2C3*0.5", required = true)
    private String expression;

    @NotNull(message = "结果类型不能为空")
    @Schema(description = "结果类型：number-数值 string-文本 percent-百分比 currency-金额", required = true)
    private String resultType;

    @NotBlank(message = "目标单元格不能为空")
    @Schema(description = "目标单元格位置，格式：行号-列号，如：3-5 表示第3行第5列", required = true)
    private String targetCell;

    @Schema(description = "依赖字段列表，如：[\"r_raw\", \"m_raw_coal\"]")
    private List<String> dependencies;

    /** ========== 模板关联 ========== */

    @NotBlank(message = "模板ID不能为空")
    @Schema(description = "模板ID（支持字符串编码或数字ID）", required = true)
    private String templateId;

    /** ========== 扩展配置（可选）========== */

    @Schema(description = "计算触发：realtime-实时 save-保存时 submit-提交时")
    private String calcTrigger = "save";

    @Schema(description = "优先级，数值越小越先计算")
    private Integer priority = 0;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status = 1;
}
