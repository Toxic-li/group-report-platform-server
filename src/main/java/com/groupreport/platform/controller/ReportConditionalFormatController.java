package com.groupreport.platform.controller;

import com.groupreport.platform.common.Result;
import com.groupreport.platform.dto.ConditionalFormatDTO;
import com.groupreport.platform.service.ReportConditionalFormatService;
import com.groupreport.platform.vo.ConditionalFormatVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 条件格式控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/report-designer/conditional-formats")
@RequiredArgsConstructor
@Tag(name = "条件格式管理", description = "报表条件格式配置与评估接口")
public class ReportConditionalFormatController {

    private final ReportConditionalFormatService formatService;

    @Operation(summary = "获取模板下的条件格式列表")
    @GetMapping("/template/{templateId}")
    public Result<List<ConditionalFormatVO>> listFormats(@PathVariable Long templateId) {
        return Result.success(formatService.getFormatsByTemplateId(templateId));
    }

    @Operation(summary = "获取条件格式详情")
    @GetMapping("/{id}")
    public Result<ConditionalFormatVO> getFormat(@PathVariable Long id) {
        return Result.success(formatService.getFormatDetail(id));
    }

    @Operation(summary = "创建条件格式规则")
    @PostMapping
    public Result<Long> createFormat(@Valid @RequestBody ConditionalFormatDTO dto) {
        return Result.success(formatService.createFormat(dto));
    }

    @Operation(summary = "更新条件格式规则")
    @PutMapping("/{id}")
    public Result<Void> updateFormat(@PathVariable Long id, @Valid @RequestBody ConditionalFormatDTO dto) {
        dto.setId(id);
        formatService.updateFormat(dto);
        return Result.success();
    }

    @Operation(summary = "删除条件格式规则")
    @DeleteMapping("/{id}")
    public Result<Void> deleteFormat(@PathVariable Long id) {
        formatService.deleteFormat(id);
        return Result.success();
    }

    @Operation(summary = "评估条件格式", description = "传入单元格数据，返回需要应用样式的单元格及样式")
    @PostMapping("/evaluate")
    public Result<List<ReportConditionalFormatService.ConditionalFormatResult>> evaluateFormats(
            @RequestParam Long templateId,
            @RequestBody Map<String, String> cellData) {
        List<ReportConditionalFormatService.ConditionalFormatResult> results =
                formatService.evaluateFormats(templateId, cellData);
        return Result.success(results);
    }
}
