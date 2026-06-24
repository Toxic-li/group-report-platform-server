package com.groupreport.platform.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.groupreport.platform.common.Constants;
import com.groupreport.platform.common.ResultCode;
import com.groupreport.platform.entity.RptAuditLog;
import com.groupreport.platform.entity.RptSubmit;
import com.groupreport.platform.exception.BusinessException;
import com.groupreport.platform.mapper.RptAuditLogMapper;
import com.groupreport.platform.mapper.RptSubmitMapper;
import com.groupreport.platform.service.ReportAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报表审核服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportAuditServiceImpl implements ReportAuditService {

    private final RptSubmitMapper submitMapper;
    private final RptAuditLogMapper auditLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long submitId, String opinion) {
        processAudit(submitId, Constants.AuditType.APPROVE, 1, opinion);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long submitId, String opinion) {
        processAudit(submitId, Constants.AuditType.REJECT, 0, opinion);
    }

    /**
     * 统一处理审核逻辑
     */
    private void processAudit(Long submitId, int auditType, int result, String opinion) {
        Long auditorId = StpUtil.getLoginIdAsLong();

        // 1. 获取提交记录
        RptSubmit submit = submitMapper.selectById(submitId);
        if (submit == null) {
            throw new BusinessException(ResultCode.SUBMIT_NOT_FOUND);
        }

        // 2. 检查状态
        if (submit.getSubmitStatus() != Constants.SubmitStatus.PENDING) {
            throw new BusinessException("当前状态不允许此操作");
        }

        int fromStatus = submit.getSubmitStatus();

        // 3. 更新提交状态
        if (result == 1) { // 通过
            submit.setSubmitStatus(Constants.SubmitStatus.APPROVED);
        } else { // 驳回
            submit.setSubmitStatus(Constants.SubmitStatus.REJECTED);
        }
        submit.setAuditTime(LocalDateTime.now());
        submit.setAuditorId(auditorId);
        submit.setAuditRemark(opinion);
        submitMapper.updateById(submit);

        // 4. 记录审核日志
        RptAuditLog auditLog = new RptAuditLog();
        auditLog.setSubmitId(submitId);
        auditLog.setTemplateId(submit.getTemplateId());
        auditLog.setOrgId(submit.getOrgId());
        auditLog.setAuditType(auditType);
        auditLog.setFromStatus(fromStatus);
        auditLog.setToStatus(submit.getSubmitStatus());
        auditLog.setAuditorId(auditorId);
        auditLog.setAuditorName(getCurrentUserName()); // TODO: 从缓存获取用户名
        auditLog.setAuditResult(result);
        auditLog.setAuditOpinion(opinion);
        auditLog.setAuditTime(LocalDateTime.now());
        auditLogMapper.insert(auditLog);

        log.info("审核报表: submitId={}, type={}, result={}, auditor={}",
                submitId, auditType, result, auditorId);
    }

    @Override
    public List<?> getPendingAudits() {
        LambdaQueryWrapper<RptSubmit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RptSubmit::getSubmitStatus, Constants.SubmitStatus.PENDING)
               .orderByAsc(RptSubmit::getSubmitTime);
        return submitMapper.selectList(wrapper);
    }

    private String getCurrentUserName() {
        try {
            Object loginId = StpUtil.getLoginId();
            return loginId != null ? loginId.toString() : "系统";
        } catch (Exception e) {
            return "系统";
        }
    }
}
