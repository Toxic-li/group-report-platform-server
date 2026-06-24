package com.groupreport.platform.controller;

import com.groupreport.platform.common.Result;
import com.groupreport.platform.dto.DesignerTemplateDTO;
import com.groupreport.platform.service.*;
import com.groupreport.platform.vo.ReportDesignerTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 报表设计器统一入口 — 低代码设计器V2 核心控制器
 *
 * 前端设计器 ↔ 后端引擎 之间的唯一数据通道
 * 一次请求获取/保存完整的模板JSON定义
 */
@Slf4j
@RestController
@RequestMapping("/api/report-designer")
@RequiredArgsConstructor
@Tag(name = "报表设计器", description = "低代码报表设计器统一JSON接口（前端Univer对接核心）")
public class ReportDesignerController {

    private final ReportDesignerService designerService;

    /**
     * 加载完整模板JSON（设计器打开时调用）
     * 返回结构：{ id, name, code, version, status, layout, rowTree, columnTree,
     *            metrics, aggregates, validators, conditionalFormats, dataSource }
     */
    @Operation(summary = "加载完整模板JSON", description = "一次请求获取模板全部配置，供设计器渲染")
    @GetMapping("/template/{templateId}")
    public Result<ReportDesignerTemplateVO> loadTemplate(@PathVariable Long templateId) {
        ReportDesignerTemplateVO vo = designerService.loadFullTemplate(templateId);
        return Result.success(vo);
    }

    /**
     * 保存完整模板JSON（设计器保存时调用）
     * 前端将整个设计结果作为JSON提交，后端拆分到各张表
     */
    @Operation(summary = "保存完整模板JSON", description = "将设计器的完整配置一次性保存到后端")
    @PostMapping("/template")
    public Result<Long> saveTemplate(@Valid @RequestBody DesignerTemplateDTO dto) {
        Long id = designerService.saveTemplate(dto);
        return Result.success(id);
    }

    /**
     * 更新完整模板JSON
     */
    @Operation(summary = "更新完整模板JSON")
    @PutMapping("/template/{templateId}")
    public Result<Void> updateTemplate(@PathVariable Long templateId,
                                       @Valid @RequestBody ReportDesignerTemplateVO templateVO) {
        templateVO.setId(String.valueOf(templateId));
        designerService.updateFullTemplate(templateId, templateVO);
        return Result.success();
    }

    /**
     * 发布模板（含校验、版本快照）
     */
    @Operation(summary = "发布模板", description = "执行完整性校验后发布模板")
    @PostMapping("/template/{templateId}/publish")
    public Result<Void> publishTemplate(@PathVariable Long templateId) {
        designerService.publishDesign(templateId);
        return Result.success();
    }

    /**
     * 复制模板（基于JSON克隆）
     */
    @Operation(summary = "复制模板", description = "基于现有模板创建副本（包含行列/公式/校验/条件格式等）")
    @PostMapping("/template/{templateId}/copy")
    public Result<Long> copyTemplate(@PathVariable Long templateId,
                                     @RequestParam(required = false) String newName) {
        Long newId = designerService.copyTemplate(templateId, newName);
        return Result.success(newId);
    }

    /**
     * 导出模板为JSON文件下载
     */
    @Operation(summary = "导出模板JSON", description = "导出完整模板定义为可迁移的JSON")
    @GetMapping("/template/{templateId}/export")
    public Result<ReportDesignerTemplateVO> exportTemplate(@PathVariable Long templateId) {
        ReportDesignerTemplateVO vo = designerService.loadFullTemplate(templateId);
        return Result.success(vo);
    }

    /**
     * 从JSON导入模板
     */
    @Operation(summary = "导入模板JSON", description="从导出的JSON恢复模板")
    @PostMapping("/template/import")
    public Result<Long> importTemplate(@Valid @RequestBody ReportDesignerTemplateVO templateVO) {
        Long templateId = designerService.importFromJson(templateVO);
        return Result.success(templateId);
    }

    // ==================== 设计器辅助接口 ====================

    // 数据源列表已统一由 DataSourceController (/api/report-designer/data-sources) 提供

    /**
     * 预览模板效果（返回示例数据+计算结果）
     */
    @Operation(summary = "预览模板", description = "使用示例数据预览模板渲染效果")
    @PostMapping("/template/{templateId}/preview")
    public Result<ReportDesignerTemplateVO.PreviewResult> previewTemplate(
            @PathVariable Long templateId,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String period) {
        var result = designerService.previewTemplate(templateId, orgId, period);
        return Result.success(result);
    }
}
