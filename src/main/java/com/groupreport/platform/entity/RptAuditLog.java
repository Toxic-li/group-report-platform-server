package com.groupreport.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审核记录实体
 */
@Data
@TableName("rpt_audit_log")
public class RptAuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 提交记录ID */
    private Long submitId;

    /** 模板ID */
    private Long templateId;

    /** 组织ID */
    private Long orgId;

    /** 审核类型：1-提交审核 2-审批通过 3-审批驳回 4-撤回 5-重新提交 */
    private Integer auditType;

    /** 原状态 */
    private Integer fromStatus;

    /** 目标状态 */
    private Integer toStatus;

    /** 审核人ID */
    private Long auditorId;

    /** 审核人姓名 */
    private String auditorName;

    /** 审核结果：0-拒绝 1-通过 */
    private Integer auditResult;

    /** 审核意见 */
    private String auditOpinion;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 附件 */
    private String attachmentUrl;

    /** 备注 */
    private String remark;
}
