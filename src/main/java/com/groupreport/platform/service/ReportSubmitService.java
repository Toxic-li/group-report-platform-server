package com.groupreport.platform.service;

import com.groupreport.platform.common.PageResult;
import com.groupreport.platform.dto.ReportDataSaveDTO;
import com.groupreport.platform.vo.ReportDataVO;

/**
 * 报表提交服务接口
 */
public interface ReportSubmitService {

    /**
     * 提交报表审核
     * @param templateId 模板ID
     * @param orgId 组织ID
     * @param period 周期
     * @param remark 备注
     * @return 提交记录ID
     */
    Long submitForAudit(Long templateId, Long orgId, String period, String remark);

    /**
     * 撤回提交
     * @param submitId 提交记录ID
     */
    void withdrawSubmit(Long submitId);

    /**
     * 分页查询提交记录
     * @param templateId 模板ID（可选）
     * @param orgId 组织ID（可选）
     * @param status 状态（可选）
     * @param current 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<?> pageSubmits(Long templateId, Long orgId, Integer status,
                               long current, long size);

    /**
     * 获取提交详情（含数据）
     * @param submitId 提交记录ID
     * @return 提交详情
     */
    ReportDataVO getSubmitDetail(Long submitId);
}
