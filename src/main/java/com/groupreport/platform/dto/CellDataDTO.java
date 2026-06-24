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

    @Schema(description = "行编码", required = true)
    private String rowCode;

    @Schema(description = "列编码", required = true)
    private String columnCode;

    @Schema(description = "文本值")
    private String valueText;

    @Schema(description = "数值")
    private BigDecimal valueNumber;

    @Schema(description = "数据类型：1-文本 2-数字 3-日期")
    private Integer dataType = 1;
}
