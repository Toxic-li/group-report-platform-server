package com.groupreport.platform.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.groupreport.platform.common.Constants;
import com.groupreport.platform.common.PageResult;
import com.groupreport.platform.common.ResultCode;
import com.groupreport.platform.entity.RptSubmit;
import com.groupreport.platform.exception.BusinessException;
import com.groupreport.platform.mapper.RptDataMapper;
import com.groupreport.platform.mapper.RptSubmitMapper;
import com.groupreport.platform.service.ReportDataService;
import com.groupreport.platform.service.ReportSubmitService;
import com.groupreport.platform.vo.ReportDataVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报表提交服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportSubmitServiceImpl implements ReportSubmitService {

    private final RptSubmitMapper submitMapper;
    private final RptDataMapper dataMapper;
    private final ReportDataService dataService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitForAudit(Long templateId, Long orgId, String period, String remark) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 1. 获取提交记录（草稿或驳回状态）
        LambdaQueryWrapper<RptSubmit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RptSubmit::getTemplateId, templateId)
               .eq(RptSubmit::getOrgId, orgId)
               .eq(RptSubmit::getPeriod, period)
               .in(RptSubmit::getSubmitStatus,
                       Constants.SubmitStatus.DRAFT,
                       Constants.SubmitStatus.REJECTED)
               .orderByDesc(RptSubmit::getCreateTime)
               .last("LIMIT 1");

        RptSubmit submit = submitMapper.selectOne(wrapper);

        if (submit == null) {
            // 创建新的提交记录
            submit = new RptSubmit();
            submit.setSubmitNo("SUB" + System.currentTimeMillis());
            submit.setTemplateId(templateId);
            submit.setOrgId(orgId);
            submit.setPeriod(period);
            submit.setPeriodType(getPeriodType(templateId));
            submit.setCreateBy(userId);
            submitMapper.insert(submit);
        }

        // 2. 检查状态是否允许提交
        if (submit.getSubmitStatus() == Constants.SubmitStatus.PENDING) {
            throw new BusinessException(ResultCode.PERIOD_ALREADY_SUBMITTED);
        }
        if (submit.getSubmitStatus() == Constants.SubmitStatus.APPROVED) {
            throw new BusinessException("该周期数据已审核通过，无需重复提交");
        }

        // 3. 计算数据完整率
        ReportDataVO reportData = dataService.getReportData(templateId, orgId, period);
        BigDecimal completeRate = BigDecimal.valueOf(reportData.getCompleteRate());

        // 4. 更新提交记录
        submit.setSubmitStatus(Constants.SubmitStatus.PENDING);
        submit.setSubmitTime(LocalDateTime.now());
        submit.setDataCompleteRate(completeRate);
        submit.setTotalRows(reportData.getTotalCells());
        submit.setFilledRows(reportData.getFilledCells());
        submit.setRemark(remark);
        submit.setUpdateBy(userId);
        submitMapper.updateById(submit);

        log.info("提交报表审核: submitId={}, template={}, org={}, period={}",
                submit.getId(), templateId, orgId, period);

        return submit.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawSubmit(Long submitId) {
        RptSubmit submit = submitMapper.selectById(submitId);
        if (submit == null) {
            throw new BusinessException(ResultCode.SUBMIT_NOT_FOUND);
        }

        if (submit.getSubmitStatus() != Constants.SubmitStatus.PENDING) {
            throw new BusinessException(ResultCode.STATUS_NOT_ALLOWED);
        }

        submit.setSubmitStatus(Constants.SubmitStatus.WITHDRAWN);
        submit.setUpdateBy(StpUtil.getLoginIdAsLong());
        submitMapper.updateById(submit);

        log.info("撤回报表提交: submitId={}", submitId);
    }

    @Override
    public PageResult<?> pageSubmits(Long templateId, Long orgId, Integer status,
                                      long current, long size) {
        Page<RptSubmit> page = new Page<>(current, size);
        LambdaQueryWrapper<RptSubmit> wrapper = new LambdaQueryWrapper<>();

        if (templateId != null) {
            wrapper.eq(RptSubmit::getTemplateId, templateId);
        }
        if (orgId != null) {
            wrapper.eq(RptSubmit::getOrgId, orgId);
        }
        if (status != null) {
            wrapper.eq(RptSubmit::getSubmitStatus, status);
        }

        wrapper.orderByDesc(RptSubmit::getCreateTime);
        Page<RptSubmit> resultPage = submitMapper.selectPage(page, wrapper);

        return PageResult.of(resultPage);
    }

    @Override
    public ReportDataVO getSubmitDetail(Long submitId) {
        RptSubmit submit = submitMapper.selectById(submitId);
        if (submit == null) {
            throw new BusinessException(ResultCode.SUBMIT_NOT_FOUND);
        }

        return dataService.getReportData(
                submit.getTemplateId(),
                submit.getOrgId(),
                submit.getPeriod()
        );
    }

    private Integer getPeriodType(Long templateId) {
        // 简化处理，实际应从模板获取
        return Constants.PeriodType.MONTHLY;
    }
}
