package com.groupreport.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 单元格值VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "单元格值")
public class CellValueVO {

    @Schema(description = "文本值")
    private String text;

    @Schema(description = "数值")
    private BigDecimal number;

    @Schema(description = "数据类型：1-文本 2-数字 3-日期")
    private Integer dataType;

    @Schema(description = "是否公式计算值")
    private Boolean isFormula;

    @Schema(description = "是否被修改")
    private Boolean isModified;

    @Schema(description = "数据来源：1-手动录入 2-公式计算 3-系统导入 4-接口同步")
    private Integer source;
}
