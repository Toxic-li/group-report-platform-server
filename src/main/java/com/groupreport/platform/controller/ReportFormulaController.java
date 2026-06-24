package com.groupreport.platform.controller;

import com.groupreport.platform.common.Result;
import com.groupreport.platform.dto.FormulaDTO;
import com.groupreport.platform.service.ReportFormulaService;
import com.groupreport.platform.vo.FormulaVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 公式管理控制器 — 对齐前端 Univer 接口
 */
@Slf4j
@RestController
@RequestMapping("/api/report-designer/formulas")
@RequiredArgsConstructor
@Tag(name = "公式管理", description = "报表公式配置与计算接口（前端Univer对接）")
public class ReportFormulaController {

    private final ReportFormulaService formulaService;

    @Operation(summary = "获取模板下的公式列表")
    @GetMapping("/template/{templateId}")
    public Result<List<FormulaVO>> listFormulas(@PathVariable String templateId) {
        // 支持字符串编码或数字ID
        Long tplId = resolveTemplateId(templateId);
        List<FormulaVO> list = formulaService.getFormulasByTemplateId(tplId);
        return Result.success(list);
    }

    @Operation(summary = "获取公式详情")
    @GetMapping("/{id}")
    public Result<FormulaVO> getFormula(@PathVariable Long id) {
        FormulaVO vo = formulaService.getFormulaDetail(id);
        return Result.success(vo);
    }

    @Operation(summary = "创建公式", description = "前端Univer传输格式: fieldName/label/expression/resultType/targetCell/dependencies/templateId")
    @PostMapping
    public Result<Long> createFormula(@Valid @RequestBody FormulaDTO dto) {
        Long id = formulaService.createFormula(dto);
        return Result.success(id);
    }

    @Operation(summary = "更新公式")
    @PutMapping("/{id}")
    public Result<Void> updateFormula(@PathVariable Long id, @Valid @RequestBody FormulaDTO dto) {
        dto.setId(id);
        formulaService.updateFormula(dto);
        return Result.success();
    }

    @Operation(summary = "删除公式")
    @DeleteMapping("/{id}")
    public Result<Void> deleteFormula(@PathVariable Long id) {
        formulaService.deleteFormula(id);
        return Result.success();
    }

    @Operation(summary = "启用/禁用公式")
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        formulaService.updateStatus(id, status);
        return Result.success();
    }

    // ==================== 公式计算接口 ====================

    @Operation(summary = "批量执行公式计算", description = "按优先级排序批量执行指定触发类型的公式")
    @PostMapping("/calc/batch")
    public Result<Map<String, String>> batchCalculate(
            @RequestParam String templateId,
            @RequestParam Long orgId,
            @RequestParam String period,
            @RequestParam(defaultValue = "save") String calcTrigger) {
        Long tplId = resolveTemplateId(templateId);
        int triggerInt = mapCalcTrigger(calcTrigger);
        Map<String, String> results = formulaService.executeFormulas(tplId, orgId, period, triggerInt);
        return Result.success(results);
    }

    @Operation(summary = "执行单个公式计算", description = "根据公式ID执行单个公式的计算并保存结果")
    @PostMapping("/calc/{formulaId}")
    public Result<String> singleCalculate(
            @PathVariable Long formulaId,
            @RequestParam Long orgId,
            @RequestParam String period) {
        String result = formulaService.executeSingleFormula(formulaId, orgId, period);
        return Result.success(result);
    }

    // ==================== 辅助方法 ====================

    private Long resolveTemplateId(String templateIdStr) {
        try {
            return Long.parseLong(templateIdStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("模板ID格式错误: " + templateIdStr);
        }
    }

    private int mapCalcTrigger(String trigger) {
        return switch (trigger.toLowerCase()) {
            case "realtime" -> 1;
            case "save" -> 2;
            case "submit" -> 3;
            default -> 2;
        };
    }
}
