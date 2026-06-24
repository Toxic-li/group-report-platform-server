package com.groupreport.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 条件格式实体
 */
@Data
@TableName("rpt_conditional_format")
public class RptConditionalFormat implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 模板ID */
    private Long templateId;

    /** 规则名称 */
    private String formatName;

    /** 条件类型：1-单元格值 2-公式 3-前N名 4-重复值 5-空值 */
    private Integer conditionType;

    /** 运算符：eq/ne/gt/ge/lt/le/between/contains */
    private String operator;

    /** 条件值1（JSON存储，支持数字/字符串/引用） */
    private String conditionValue;

    /** 条件值2（between时使用） */
    private String conditionValue2;

    /** 应用范围，如：R1C1:R10C5 */
    private String applyRange;

    /** 样式配置JSON：backgroundColor/color/bold/italic/fontSize */
    private String styleConfig;

    /** 停用条件格式（优先级低于此规则的将被覆盖） */
    private Boolean stopIfTrue;

    /** 排序号 */
    private Integer sortOrder;

    /** 状态：0-禁用 1-启用 */
    private Integer status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
