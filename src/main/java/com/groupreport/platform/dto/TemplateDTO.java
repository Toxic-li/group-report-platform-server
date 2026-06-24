package com.groupreport.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 报表模板DTO
 */
@Data
@Schema(description = "报表模板信息")
public class TemplateDTO {

    @Schema(description = "模板ID（新增时为空）")
    private Long id;

    @NotBlank(message = "模板编码不能为空")
    @Schema(description = "模板编码", required = true)
    private String templateCode;

    @NotBlank(message = "模板名称不能为空")
    @Schema(description = "模板名称", required = true)
    private String templateName;

    @Schema(description = "模板类型：1-统计报表 2-填报报表 3-汇总报表")
    private Integer templateType = 1;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "模板描述")
    private String description;

    @Schema(description = "周期类型：1-日 2-周 3-月 4-季 5-年")
    private Integer periodType = 3;

    @Schema(description = "是否需要审核")
    private Integer auditRequired = 1;

    @Schema(description = "是否启用公式")
    private Integer formulaEnabled = 0;

    @Schema(description = "行配置列表")
    private List<RowConfigDTO> rows;

    @Schema(description = "列配置列表")
    private List<ColumnConfigDTO> columns;
}
