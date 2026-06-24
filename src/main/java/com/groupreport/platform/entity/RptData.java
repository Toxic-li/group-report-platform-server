package com.groupreport.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 报表数据实体（通用结构）
 */
@Data
@TableName("rpt_data")
public class RptData implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 提交记录ID */
    private Long submitId;

    /** 模板ID */
    private Long templateId;

    /** 组织ID */
    private Long orgId;

    /** 周期 */
    private String period;

    /** 行结构ID */
    private Long rowId;

    /** 行编码 */
    private String rowCode;

    /** 列结构ID */
    private Long columnId;

    /** 列编码 */
    private String columnCode;

    /** 文本值 */
    private String valueText;

    /** 数值 */
    private java.math.BigDecimal valueNumber;

    /** 日期值 */
    private java.time.LocalDate valueDate;

    /** 数据类型：1-文本 2-数字 3-日期 */
    private Integer dataType;

    /** 是否公式计算值 */
    private Integer isFormula;

    /** 是否被修改 */
    private Integer isModified;

    /** 数据来源：1-手动录入 2-公式计算 3-系统导入 4-接口同步 */
    private Integer source;

    /** 备注 */
    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
