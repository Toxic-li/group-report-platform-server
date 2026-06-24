package com.groupreport.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 列配置DTO
 */
@Data
@Schema(description = "列配置")
public class ColumnConfigDTO {

    @Schema(description = "列编码（新增时自动生成或手动指定）")
    private String columnCode;

    @NotBlank(message = "列名称不能为空")
    @Schema(description = "列名称", required = true)
    private String columnName;

    @Schema(description = "列类型：1-文本 2-数字 3-日期 4-下拉选择 5-公式 6-只读")
    private Integer columnType = 1;

    @Schema(description = "数据类型：1-字符串 2-整数 3-小数 4-百分比 5-金额")
    private Integer dataType = 1;

    @Schema(description = "父列编码（用于合并单元格等场景）")
    private String parentColumnCode;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "列宽（像素）")
    private Integer width = 100;

    @Schema(description = "小数位数")
    private Integer decimalPlaces = 2;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "是否必填")
    private Integer required = 0;

    @Schema(description = "是否只读")
    private Integer readonly = 0;

    @Schema(description = "默认值")
    private String defaultValue;

    @Schema(description = "最小值")
    private BigDecimal minValue;

    @Schema(description = "最大值")
    private BigDecimal maxValue;

    @Schema(description = "格式化模式")
    private String formatPattern;

    @Schema(description = "对齐方式：0-左 1-中 2-右")
    private Integer align;
}
