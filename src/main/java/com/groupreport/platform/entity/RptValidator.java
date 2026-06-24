package com.groupreport.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 校验规则实体
 */
@Data
@TableName("rpt_validator")
public class RptValidator implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 模板ID */
    private Long templateId;

    /** 规则名称 */
    private String validatorName;

    /** 校验类型：1-非空 2-范围 3-正则 4-自定义 5-业务规则 */
    private Integer validatorType;

    /** 目标行编码列表 */
    private String targetRows;

    /** 目标列编码列表 */
    private String targetColumns;

    /** 规则配置JSON */
    private String ruleConfig;

    /** 错误提示信息 */
    private String errorMessage;

    /** 校验时机：1-输入时 2-保存时 3-提交时 */
    private Integer validateTrigger;

    /** 优先级 */
    private Integer priority;

    /** 状态：0-禁用 1-启用 */
    private Integer status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
