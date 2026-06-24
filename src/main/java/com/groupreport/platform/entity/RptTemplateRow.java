package com.groupreport.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 模板行结构实体
 */
@Data
@TableName("rpt_template_row")
public class RptTemplateRow implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 模板ID */
    private Long templateId;

    /** 行编码 */
    private String rowCode;

    /** 行名称/标题 */
    private String rowName;

    /** 行类型：1-数据行 2-标题行 3-合计行 4-分组头 */
    private Integer rowType;

    /** 父行ID */
    private Long parentId;

    /** 排序号 */
    private Integer sortOrder;

    /** 层级 */
    private Integer level;

    /** 缩进级别 */
    private Integer indent;

    /** 是否可展开 */
    private Integer isExpandable;

    /** 是否合计行 */
    private Integer isSummary;

    /** 合计公式 */
    private String summaryFormula;

    /** 背景颜色 */
    private String backgroundColor;

    /** 是否加粗 */
    private Integer fontBold;

    /** 是否可见 */
    private Integer visible;

    /** 是否冻结 */
    private Integer frozen;

    /** 行高 */
    private Integer height;

    /** 扩展配置 */
    private String configJson;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
