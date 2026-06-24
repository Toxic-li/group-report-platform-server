package com.groupreport.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 报表数据保存DTO
 */
@Data
@Schema(description = "报表数据保存请求")
public class ReportDataSaveDTO {

    @NotNull(message = "模板ID不能为空")
    @Schema(description = "模板ID", required = true)
    private Long templateId;

    @NotNull(message = "组织ID不能为空")
    @Schema(description = "组织ID", required = true)
    private Long orgId;

    @NotBlank(message = "填报周期不能为空")
    @Schema(description = "填报周期，如：202401,2024Q1", required = true)
    private String period;

    @Schema(description = "数据列表（单元格数据）")
    private List<CellDataDTO> cells;

    @Schema(description = "备注")
    private String remark;
}
