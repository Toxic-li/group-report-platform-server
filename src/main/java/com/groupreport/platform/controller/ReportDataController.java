package com.groupreport.platform.controller;

import com.groupreport.platform.common.Result;
import com.groupreport.platform.dto.ReportDataSaveDTO;
import com.groupreport.platform.service.ReportDataService;
import com.groupreport.platform.vo.ReportDataVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 报表数据控制器（统一入口：/report-designer/data）
 */
@Tag(name = "报表数据", description = "报表数据的填报、保存、查询接口")
@RestController
@RequestMapping("/report-designer/data")
@RequiredArgsConstructor
public class ReportDataController {

    private final ReportDataService dataService;

    @Operation(summary = "获取报表数据（用于前端Univer渲染）")
    @GetMapping
    public Result<ReportDataVO> getReportData(
            @Parameter(description = "模板ID") @RequestParam Long templateId,
            @Parameter(description = "组织ID") @RequestParam Long orgId,
            @Parameter(description = "周期，如202401") @RequestParam String period) {
        return Result.success(dataService.getReportData(templateId, orgId, period));
    }

    @Operation(summary = "保存报表数据", description = "支持草稿/自动保存/批量保存，传入完整单元格列表一次性upsert")
    @PostMapping("/save")
    public Result<Map<String, Object>> saveData(@Valid @RequestBody ReportDataSaveDTO saveDTO) {
        return Result.success(dataService.saveData(saveDTO));
    }

    @Operation(summary = "获取单元格值")
    @GetMapping("/cell")
    public Result<Object> getCellValue(
            @Parameter(description = "模板ID") @RequestParam Long templateId,
            @Parameter(description = "组织ID") @RequestParam Long orgId,
            @Parameter(description = "周期") @RequestParam String period,
            @Parameter(description = "行编码") @RequestParam String rowCode,
            @Parameter(description = "列编码") @RequestParam String columnCode) {
        return Result.success(dataService.getCellValue(templateId, orgId, period, rowCode, columnCode));
    }

    @Operation(summary = "清空报表数据")
    @DeleteMapping
    public Result<Void> clearData(
            @Parameter(description = "模板ID") @RequestParam Long templateId,
            @Parameter(description = "组织ID") @RequestParam Long orgId,
            @Parameter(description = "周期") @RequestParam String period) {
        dataService.clearData(templateId, orgId, period);
        return Result.success();
    }

    @Operation(summary = "校验报表数据")
    @PostMapping("/validate")
    public Result<List<String>> validateData(
            @Parameter(description = "模板ID") @RequestParam Long templateId,
            @Parameter(description = "组织ID") @RequestParam Long orgId,
            @Parameter(description = "周期") @RequestParam String period) {
        return Result.success(dataService.validateData(templateId, orgId, period));
    }
}
