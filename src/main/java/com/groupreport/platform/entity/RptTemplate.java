package com.groupreport.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 报表模板实体
 */
@Data
@TableName("rpt_template")
public class RptTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 模板编码 */
    private String templateCode;

    /** 模板名称 */
    private String templateName;

    /** 模板类型：1-统计报表 2-填报报表 3-汇总报表 */
    private Integer templateType;

    /** 分类ID */
    private Long categoryId;

    /** 模板描述 */
    private String description;

    /** 当前版本号 */
    private Integer version;

    /** 状态：0-草稿 1-已发布 2-已停用 */
    private Integer status;

    /** 是否公开 */
    private Integer isPublic;

    /** 是否允许导出 */
    private Integer allowExport;

    /** 是否允许导入 */
    private Integer allowImport;

    /** 周期类型：1-日 2-周 3-月 4-季 5-年 */
    private Integer periodType;

    /** 填报截止时间（小时） */
    private Integer submitDeadline;

    /** 是否需要审核 */
    private Integer auditRequired;

    /** 是否启用公式 */
    private Integer formulaEnabled;

    /** 行数 */
    private Integer rowCount;

    /** 列数 */
    private Integer columnCount;

    /** 扩展配置JSON */
    private String configJson;

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
