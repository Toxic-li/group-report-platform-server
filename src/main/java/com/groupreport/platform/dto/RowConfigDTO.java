package com.groupreport.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 行配置DTO
 */
@Data
@Schema(description = "行配置")
public class RowConfigDTO {

    @Schema(description = "行编码（新增时自动生成或手动指定）")
    private String rowCode;

    @NotBlank(message = "行名称不能为空")
    @Schema(description = "行名称", required = true)
    private String rowName;

    @Schema(description = "行类型：1-数据行 2-标题行 3-合计行 4-分组头")
    private Integer rowType = 1;

    @Schema(description = "父行编码（用于树形结构）")
    private String parentRowCode;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "层级")
    private Integer level = 1;

    @Schema(description = "缩进级别")
    private Integer indent = 0;

    @Schema(description = "是否可展开")
    private Integer isExpandable = 0;

    @Schema(description = "是否合计行")
    private Integer isSummary = 0;

    @Schema(description = "合计公式")
    private String summaryFormula;

    @Schema(description = "背景颜色")
    private String backgroundColor;

    @Schema(description = "是否加粗")
    private Integer fontBold = 0;

    @Schema(description = "行高")
    private Integer height = 30;
}
