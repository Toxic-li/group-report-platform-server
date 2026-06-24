package com.groupreport.platform.controller;

import com.groupreport.platform.common.PageResult;
import com.groupreport.platform.common.Result;
import com.groupreport.platform.service.ReportSubmitService;
import com.groupreport.platform.vo.ReportDataVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 报表提交控制器
 */
@Tag(name = "报表提交", description = "报表提交审核相关接口")
@RestController
@RequestMapping("/api/report-designer/submit")
@RequiredArgsConstructor
public class ReportSubmitController {

    private final ReportSubmitService submitService;

    @Operation(summary = "提交报表审核")
    @PostMapping
    public Result<Long> submitForAudit(
            @Parameter(description = "模板ID") @RequestParam Long templateId,
            @Parameter(description = "组织ID") @RequestParam Long orgId,
            @Parameter(description = "周期") @RequestParam String period,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        return Result.success(submitService.submitForAudit(templateId, orgId, period, remark));
    }

    @Operation(summary = "撤回提交")
    @PutMapping("/{submitId}/withdraw")
    public Result<Void> withdrawSubmit(@PathVariable Long submitId) {
        submitService.withdrawSubmit(submitId);
        return Result.success();
    }

    @Operation(summary = "分页查询提交记录")
    @GetMapping("/page")
    public Result<PageResult<?>> pageSubmits(
            @Parameter(description = "模板ID") @RequestParam(required = false) Long templateId,
            @Parameter(description = "组织ID") @RequestParam(required = false) Long orgId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") long size) {
        return Result.success(submitService.pageSubmits(templateId, orgId, status, current, size));
    }

    @Operation(summary = "获取提交详情")
    @GetMapping("/{submitId}")
    public Result<ReportDataVO> getSubmitDetail(@PathVariable Long submitId) {
        return Result.success(submitService.getSubmitDetail(submitId));
    }
}
