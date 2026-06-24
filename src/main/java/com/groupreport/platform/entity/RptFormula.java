package com.groupreport.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公式实体
 */
@Data
@TableName("rpt_formula")
public class RptFormula implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 模板ID */
    private Long templateId;

    /** 公式名称 */
    private String formulaName;

    /** 公式表达式 */
    private String formulaExpression;

    /** 目标行编码 */
    private String targetRowCode;

    /** 目标列编码 */
    private String targetColumnCode;

    /** 公式类型：1-求和 2-平均值 3-最大值 4-最小值 5-自定义表达式 */
    private Integer formulaType;

    /** 源数据范围，如：R1C1:R10C5 */
    private String sourceRange;

    /** 计算触发：1-实时 2-保存时 3-提交时 */
    private Integer calcTrigger;

    /** 优先级，数值越小越先计算 */
    private Integer priority;

    /** 描述 */
    private String description;

    /** 状态：0-禁用 1-启用 */
    private Integer status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
