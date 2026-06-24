package com.groupreport.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 行配置VO
 */
@Data
@Schema(description = "行配置")
public class RowVO {

    @Schema(description = "行ID")
    private Long id;

    @Schema(description = "行编码")
    private String rowCode;

    @Schema(description = "行名称")
    private String rowName;

    @Schema(description = "行类型")
    private Integer rowType;

    @Schema(description = "父行ID")
    private Long parentId;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "层级")
    private Integer level;

    @Schema(description = "缩进级别")
    private Integer indent;

    @Schema(description = "是否可展开")
    private Boolean expandable;

    @Schema(description = "是否合计行")
    private Boolean summary;

    @Schema(description = "合计公式")
    private String summaryFormula;

    @Schema(description = "背景颜色")
    private String backgroundColor;

    @Schema(description = "是否加粗")
    private Boolean fontBold;

    @Schema(description = "是否可见")
    private Boolean visible;

    @Schema(description = "是否冻结")
    private Boolean frozen;

    @Schema(description = "行高")
    private Integer height;

    @Schema(description = "子行列表")
    private List<RowVO> children;
}
