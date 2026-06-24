package com.groupreport.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报表提交记录实体
 */
@Data
@TableName("rpt_submit")
public class RptSubmit implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 提交编号 */
    private String submitNo;

    /** 模板ID */
    private Long templateId;

    /** 填报组织ID */
    private Long orgId;

    /** 填报周期 */
    private String period;

    /** 周期类型 */
    private Integer periodType;

    /** 提交状态：0-草稿 1-待审核 2-已通过 3-已驳回 4-已撤回 */
    private Integer submitStatus;

    /** 提交时间 */
    private LocalDateTime submitTime;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 审核人ID */
    private Long auditorId;

    /** 审核意见 */
    private String auditRemark;

    /** 数据完整率 */
    private BigDecimal dataCompleteRate;

    /** 总行数 */
    private Integer totalRows;

    /** 已填写行数 */
    private Integer filledRows;

    /** 附件地址 */
    private String attachmentUrl;

    /** 备注 */
    private String remark;

    @TableLogic
    private Integer deleted;

    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
}
