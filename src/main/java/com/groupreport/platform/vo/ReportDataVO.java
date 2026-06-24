package com.groupreport.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 报表数据响应VO（用于前端Univer渲染）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "报表数据响应")
public class ReportDataVO {

    @Schema(description = "提交记录ID")
    private Long submitId;

    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "组织ID")
    private Long orgId;

    @Schema(description = "填报周期")
    private String period;

    @Schema(description = "提交状态")
    private Integer submitStatus;

    @Schema(description = "数据完整率")
    private Double completeRate;

    @Schema(description = "总单元格数")
    private Integer totalCells;

    @Schema(description = "已填写单元格数")
    private Integer filledCells;

    @Schema(description = "行配置")
    private List<RowVO> rows;

    @Schema(description = "列配置")
    private List<ColumnVO> columns;

    /**
     * 数据Map：key为 "rowCode:columnCode"，value为单元格值
     */
    @Schema(description = "数据内容（二维矩阵或Map形式）")
    private Map<String, CellValueVO> data;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
