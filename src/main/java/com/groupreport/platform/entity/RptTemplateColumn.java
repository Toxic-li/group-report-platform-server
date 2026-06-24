package com.groupreport.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模板列结构实体
 */
@Data
@TableName("rpt_template_column")
public class RptTemplateColumn implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 模板ID */
    private Long templateId;

    /** 列编码 */
    private String columnCode;

    /** 列名称/标题 */
    private String columnName;

    /** 列类型：1-文本 2-数字 3-日期 4-下拉选择 5-公式 6-只读 */
    private Integer columnType;

    /** 数据类型：1-字符串 2-整数 3-小数 4-百分比 5-金额 */
    private Integer dataType;

    /** 父列ID */
    private Long parentId;

    /** 排序号 */
    private Integer sortOrder;

    /** 列宽 */
    private Integer width;

    /** 小数位数 */
    private Integer decimalPlaces;

    /** 单位 */
    private String unit;

    /** 是否必填 */
    private Integer required;

    /** 是否只读 */
    private Integer readonly;

    /** 默认值 */
    private String defaultValue;

    /** 选项配置 */
    private String optionsJson;

    /** 最小值 */
    private BigDecimal minValue;

    /** 最大值 */
    private BigDecimal maxValue;

    /** 格式化模式 */
    private String formatPattern;

    /** 背景颜色 */
    private String backgroundColor;

    /** 是否加粗 */
    private Integer fontBold;

    /** 是否可见 */
    private Integer visible;

    /** 是否冻结 */
    private Integer frozen;

    /** 对齐方式 */
    private Integer align;

    /** 扩展配置 */
    private String configJson;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
