package com.groupreport.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 列配置VO
 */
@Data
@Schema(description = "列配置")
public class ColumnVO {

    @Schema(description = "列ID")
    private Long id;

    @Schema(description = "列编码")
    private String columnCode;

    @Schema(description = "列名称")
    private String columnName;

    @Schema(description = "列类型")
    private Integer columnType;

    @Schema(description = "数据类型")
    private Integer dataType;

    @Schema(description = "父列ID")
    private Long parentId;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "列宽")
    private Integer width;

    @Schema(description = "小数位数")
    private Integer decimalPlaces;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "是否必填")
    private Boolean required;

    @Schema(description = "是否只读")
    private Boolean readonly;

    @Schema(description = "默认值")
    private String defaultValue;

    @Schema(description = "最小值")
    private BigDecimal minValue;

    @Schema(description = "最大值")
    private BigDecimal maxValue;

    @Schema(description = "格式化模式")
    private String formatPattern;

    @Schema(description = "背景颜色")
    private String backgroundColor;

    @Schema(description = "是否加粗")
    private Boolean fontBold;

    @Schema(description = "是否可见")
    private Boolean visible;

    @Schema(description = "是否冻结")
    private Boolean frozen;

    @Schema(description = "对齐方式")
    private Integer align;

    @Schema(description = "选项列表（下拉选择等）")
    private List<String> options;
}
