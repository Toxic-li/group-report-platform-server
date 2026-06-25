package com.groupreport.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 单元格数据DTO
 */
@Data
@Schema(description = "单元格数据")
public class CellDataDTO {

    @Schema(description = "行ID")
    private Long rowId;

    @Schema(description = "行编码", required = true)
    private String rowCode;

    @Schema(description = "列ID")
    private Long columnId;

    @Schema(description = "列编码", required = true)
    private String columnCode;

    @Schema(description = "单元格值（字符串形式）")
    private String value;

    @Schema(description = "原始值")
    private String rawValue;

    @Schema(description = "公式")
    private String formula;

    @Schema(description = "文本值")
    private String valueText;

    @Schema(description = "数值")
    private BigDecimal valueNumber;

    @Schema(description = "数据类型：1-文本 2-数字 3-日期")
    private Integer dataType = 1;

    @Schema(description = "数据来源：1-手动录入 2-公式计算 3-系统导入 4-接口同步")
    private Integer source;

    @Schema(description = "备注")
    private String remark;
}
