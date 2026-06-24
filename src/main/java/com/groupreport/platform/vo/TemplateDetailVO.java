package com.groupreport.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 报表模板完整结构VO（用于前端Univer渲染）
 */
@Data
@Schema(description = "报表模板完整信息")
public class TemplateDetailVO {

    @Schema(description = "模板ID")
    private Long id;

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "模板类型")
    private Integer templateType;

    @Schema(description = "模板类型名称")
    private String templateTypeName;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "周期类型")
    private Integer periodType;

    @Schema(description = "周期类型名称")
    private String periodTypeName;

    @Schema(description = "是否需要审核")
    private Integer auditRequired;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "行配置列表（树形结构）")
    private List<RowVO> rows;

    @Schema(description = "列配置列表")
    private List<ColumnVO> columns;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
