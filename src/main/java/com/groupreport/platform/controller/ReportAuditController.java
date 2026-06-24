package com.groupreport.platform.controller;

import com.groupreport.platform.common.Result;
import com.groupreport.platform.service.ReportAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报表审核控制器
 */
@Tag(name = "报表审核", description = "报表审核通过/驳回接口")
@RestController
@RequestMapping("/api/report-designer/audit")
@RequiredArgsConstructor
public class ReportAuditController {

    private final ReportAuditService auditService;

    @Operation(summary = "审核通过")
    @PutMapping("/{submitId}/approve")
    public Result<Void> approve(
            @PathVariable Long submitId,
            @Parameter(description = "审核意见") @RequestParam(required = false) String opinion) {
        auditService.approve(submitId, opinion);
        return Result.success();
    }

    @Operation(summary = "审核驳回")
    @PutMapping("/{submitId}/reject")
    public Result<Void> reject(
            @PathVariable Long submitId,
            @Parameter(description = "驳回原因") @RequestParam String opinion) {
        auditService.reject(submitId, opinion);
        return Result.success();
    }

    @Operation(summary = "获取待审核列表")
    @GetMapping("/pending")
    public Result<List<?>> getPendingAudits() {
        return Result.success(auditService.getPendingAudits());
    }
}
