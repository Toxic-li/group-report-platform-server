package com.groupreport.platform.service;

import java.util.List;

/**
 * 报表审核服务接口
 */
public interface ReportAuditService {

    /**
     * 审核通过
     * @param submitId 提交记录ID
     * @param opinion 审核意见
     */
    void approve(Long submitId, String opinion);

    /**
     * 审核驳回
     * @param submitId 提交记录ID
     * @param opinion 驳回原因
     */
    void reject(Long submitId, String opinion);

    /**
     * 获取待审核列表
     * @return 待审核列表
     */
    List<?> getPendingAudits();
}
