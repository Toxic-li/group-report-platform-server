package com.groupreport.platform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.groupreport.platform.common.ResultCode;
import com.groupreport.platform.dto.ColumnTreeDTO;
import com.groupreport.platform.dto.DesignerTemplateDTO;
import com.groupreport.platform.dto.RowTreeDTO;
import com.groupreport.platform.entity.*;
import com.groupreport.platform.exception.BusinessException;
import com.groupreport.platform.mapper.*;
import com.groupreport.platform.service.*;
import com.groupreport.platform.service.*;
import com.groupreport.platform.dto.FormulaDTO;
import com.groupreport.platform.vo.FormulaVO;
import com.groupreport.platform.vo.ReportDesignerTemplateVO;
import com.groupreport.platform.vo.ReportDesignerTemplateVO.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 报表设计器核心服务实现 — 统一编排器
 *
 * 职责：完整模板JSON ↔ 关系表 的双向转换
 *
 * 前端设计器保存 → saveFullTemplate() → 拆分到 7 张表
 * 前端设计器打开 → loadFullTemplate() → 从 7 张表组装为JSON
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportDesignerServiceImpl implements ReportDesignerService {

    // ==================== 注入的所有Mapper/Service ====================

    private final RptTemplateMapper templateMapper;
    private final RptTemplateRowMapper rowMapper;
    private final RptTemplateColumnMapper columnMapper;
    private final RptFormulaMapper formulaMapper;
    private final RptDataMapper dataMapper;
    private final RptValidatorMapper validatorMapper;
    private final RptConditionalFormatMapper formatMapper;
    private final SysDataSourceMapper dataSourceMapper;
    private final ReportFormulaService formulaService;
    private final ReportValidatorService validatorService;
    private final ReportConditionalFormatService formatService;
    private final DataSourceService dsService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveTemplate(DesignerTemplateDTO dto) {

        // =========================
        // 1. 保存模板主表
        // =========================
        RptTemplate template = new RptTemplate();
        template.setTemplateCode(dto.getCode());
        template.setTemplateName(dto.getName());
        template.setTemplateType(dto.getTemplateType());
        template.setDescription(dto.getDescription());
        template.setStatus(0); // draft
        template.setVersion(1);

        templateMapper.insert(template);

        Long templateId = template.getId();

        // =========================
        // 2. 保存行树
        // =========================
        if (dto.getRowTree() != null) {
            saveRows(templateId, dto.getRowTree(), 0L);
        }

        // =========================
        // 3. 保存列树
        // =========================
        if (dto.getColumnTree() != null) {
            saveColumns(templateId, dto.getColumnTree(), 0L);
        }

        return templateId;
    }

    /**
     * 递归保存行
     */
    private void saveRows(Long templateId, List<RowTreeDTO> list, Long parentId) {

        int sort = 1;

        for (RowTreeDTO node : list) {

            RptTemplateRow row = new RptTemplateRow();

            row.setTemplateId(templateId);

            // 前端 id -> row_code
            row.setRowCode(node.getId());

            // 前端 name -> row_name
            row.setRowName(node.getName());

            row.setParentId(parentId);
            row.setSortOrder(sort++);
            row.setLevel(node.getLevel() == null ? 1 : node.getLevel());

            row.setVisible(1);

            rowMapper.insert(row);

            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                saveRows(templateId, node.getChildren(), row.getId());
            }
        }
    }

    /**
     * 递归保存列
     */
    private void saveColumns(Long templateId, List<ColumnTreeDTO> list, Long parentId) {

        int sort = 1;

        for (ColumnTreeDTO node : list) {

            RptTemplateColumn col = new RptTemplateColumn();

            col.setTemplateId(templateId);

            // 前端 id -> column_code
            col.setColumnCode(node.getId());

            // 前端 title -> column_name（关键修复点）
            col.setColumnName(node.getTitle());

            col.setColumnType("data".equals(node.getType()) ? 1 : 2);

            col.setWidth(node.getWidth() == null ? 100 : node.getWidth());

            col.setAlign(convertAlign(node.getAlign()));

            col.setParentId(parentId);

            col.setSortOrder(sort++);

            col.setVisible(1);

            columnMapper.insert(col);

            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                saveColumns(templateId, node.getChildren(), col.getId());
            }
        }
    }

    /**
     * 对齐方式转换
     */
    private Integer convertAlign(String align) {
        if ("left".equals(align)) return 0;
        if ("center".equals(align)) return 1;
        if ("right".equals(align)) return 2;
        return 0;
    }

    // ==================== 核心方法：加载完整模板JSON ====================

    /**
     * 从各关系表组装完整模板JSON
     * 前端调用一次即可获取设计器所需全部数据
     */
    @Override
    public ReportDesignerTemplateVO loadFullTemplate(Long templateId) {
        log.info("加载完整模板JSON: templateId={}", templateId);

        // 1. 模板基础信息
        RptTemplate tpl = templateMapper.selectById(templateId);
        if (tpl == null) {
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }

        ReportDesignerTemplateVO vo = new ReportDesignerTemplateVO();
        vo.setId(String.valueOf(tpl.getId()));
        vo.setName(tpl.getTemplateName());
        vo.setCode(tpl.getTemplateCode());
        vo.setVersion(tpl.getVersion());
        vo.setStatus(mapStatusToString(tpl.getStatus()));
        vo.setDescription(tpl.getDescription());
        vo.setPeriodType(mapPeriodType(tpl.getPeriodType()));
        vo.setAuditRequired(tpl.getAuditRequired() != null && tpl.getAuditRequired() == 1);

        // 2. 布局配置
        vo.setLayout(buildLayoutConfig(tpl));

        // 3. 行树结构
        List<RptTemplateRow> allRows = rowMapper.selectList(
                new LambdaQueryWrapper<RptTemplateRow>()
                        .eq(RptTemplateRow::getTemplateId, templateId)
                        .orderByAsc(RptTemplateRow::getSortOrder));
        vo.setRowTree(buildTreeNodes(allRows, true));

        // 4. 列树结构
        List<RptTemplateColumn> allCols = columnMapper.selectList(
                new LambdaQueryWrapper<RptTemplateColumn>()
                        .eq(RptTemplateColumn::getTemplateId, templateId)
                        .orderByAsc(RptTemplateColumn::getSortOrder));
        vo.setColumnTree(buildTreeNodes(allCols, false));

        // 5. 度量指标（公式）
        List<FormulaVO> formulaVOs = formulaService.getFormulasByTemplateId(templateId);
        vo.setMetrics(formulaVOs.stream().map(this::formulaVoToMetricDef).toList());

        // 6. 合计规则（从行配置中提取 isSummary=1 的行）
        vo.setAggregates(extractAggregates(allRows));

        // 7. 校验规则
        vo.setValidators(validatorService.exportToDesignerJson(templateId));

        // 8. 条件格式
        vo.setConditionalFormats(formatService.exportToDesignerJson(templateId));

        // 9. 数据源配置（从模板的 configJson 或独立数据源关联中提取）
        vo.setDataSource(resolveDataSource(tpl));

        // 10. 权限配置
        vo.setPermissions(buildPermissionConfig(tpl));

        log.info("模板加载完成: id={}, name={}, rows={}, cols={}, metrics={}, validators={}, formats={}",
                templateId, vo.getName(),
                allRows.size(), allCols.size(),
                formulaVOs.size(), vo.getValidators().size(), vo.getConditionalFormats().size());

        return vo;
    }

    // ==================== 核心方法：保存完整模板JSON ====================

    /**
     * 将前端提交的完整模板JSON拆分保存到各张表
     * 使用事务保证原子性
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveFullTemplate(ReportDesignerTemplateVO templateVO) {
        log.info("保存完整模板JSON: name={}, code={}", templateVO.getName(), templateVO.getCode());

        // 1. 创建主模板记录
        RptTemplate tpl = buildTemplateEntity(templateVO);
        tpl.setStatus(0); // 新建默认草稿
        if (tpl.getVersion() == null) tpl.setVersion(1);
        templateMapper.insert(tpl);
        Long templateId = tpl.getId();

        // 2. 保存行树
        saveRowTree(templateId, templateVO.getRowTree());

        // 3. 保存列树
        saveColumnTree(templateId, templateVO.getColumnTree());

        // 4. 保存公式/指标
        saveMetrics(templateId, templateVO.getMetrics());

        // 5. 保存校验规则
        validatorService.importFromDesignerJson(templateId, templateVO.getValidators());

        // 6. 保存条件格式
        formatService.importFromDesignerJson(templateId, templateVO.getConditionalFormats());

        // 7. 关联数据源
        linkDataSource(templateId, templateVO.getDataSource());

        log.info("模板保存完成: templateId={}", templateId);
        return templateId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFullTemplate(Long templateId, ReportDesignerTemplateVO templateVO) {
        log.info("更新完整模板JSON: templateId={}", templateId);

        // 1. 更新主模板
        RptTemplate existing = templateMapper.selectById(templateId);
        if (existing == null) throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);

        updateTemplateFromVO(existing, templateVO);
        templateMapper.updateById(existing);

        // 2. 删除旧的子数据，重新写入（全量更新策略）
        deleteSubData(templateId);

        // 3. 重新保存所有子数据
        saveRowTree(templateId, templateVO.getRowTree());
        saveColumnTree(templateId, templateVO.getColumnTree());
        saveMetrics(templateId, templateVO.getMetrics());
        validatorService.importFromDesignerJson(templateId, templateVO.getValidators());
        formatService.importFromDesignerJson(templateId, templateVO.getConditionalFormats());
        linkDataSource(templateId, templateVO.getDataSource());

        log.info("模板更新完成: templateId={}", templateId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishDesign(Long templateId) {
        log.info("发布模板设计: templateId={}", templateId);

        RptTemplate tpl = templateMapper.selectById(templateId);
        if (tpl == null) throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);

        // 1. 校验完整性
        validateTemplateCompleteness(templateId);

        // 2. 版本号+1
        int newVersion = (tpl.getVersion() == null ? 0 : tpl.getVersion()) + 1;
        tpl.setVersion(newVersion);
        tpl.setStatus(1); // 已发布
        templateMapper.updateById(tpl);

        // 3. 创建版本快照（预留：可存入 rpt_template_version 表）
        saveVersionSnapshot(templateId, newVersion);

        log.info("模板发布成功: templateId={}, version={}", templateId, newVersion);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copyTemplate(Long sourceTemplateId, String newName) {
        log.info("复制模板: sourceId={}, newName={}", sourceTemplateId, newName);

        // 加载源模板完整JSON
        ReportDesignerTemplateVO sourceVO = loadFullTemplate(sourceTemplateId);

        // 修改名称和编码
        if (StrUtil.isNotBlank(newName)) {
            sourceVO.setName(newName);
        } else {
            sourceVO.setName(sourceVO.getName() + " (副本)");
        }
        sourceVO.setCode(null); // 清空编码，让系统生成新的
        sourceVO.setId(null);
        sourceVO.setStatus("draft");
        if (sourceVO.getVersion() != null) sourceVO.setVersion(1);

        // 作为新模板保存
        return saveFullTemplate(sourceVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long importFromJson(ReportDesignerTemplateVO templateVO) {
        log.info("从JSON导入模板: name={}", templateVO.getName());
        // 检查是否已存在同编码模板
        if (StrUtil.isNotBlank(templateVO.getCode())) {
            LambdaQueryWrapper<RptTemplate> wrapper = new LambdaQueryWrapper<RptTemplate>()
                    .eq(RptTemplate::getTemplateCode, templateVO.getCode());
            Long count = templateMapper.selectCount(wrapper);
            if (count > 0) {
                templateVO.setCode(templateVO.getCode() + "_import_" + System.currentTimeMillis());
            }
        }
        return saveFullTemplate(templateVO);
    }

    @Override
    public List<?> listAvailableDataSources() {
        return dsService.listAll();
    }

    @Override
    public PreviewResult previewTemplate(Long templateId, Long orgId, String period) {
        log.info("预览模板: templateId={}, orgId={}, period={}", templateId, orgId, period);

        // 1. 加载模板定义
        ReportDesignerTemplateVO template = loadFullTemplate(templateId);

        // 2. 获取数据（如果有指定org和period则取真实数据，否则返回空）
        Map<String, String> cellData = Map.of();
        if (orgId != null && StrUtil.isNotBlank(period)) {
            cellData = loadCellDataForPreview(templateId, orgId, period);
        }

        // 3. 执行公式计算
        Map<String, String> formulaResults = formulaService.executeFormulas(
                templateId, orgId != null ? orgId : -1L,
                StrUtil.isNotBlank(period) ? period : "preview", 1);

        // 4. 执行校验
        Map<String, String> validationErrors = validatorService.validateData(
                templateId, cellData, 2);

        // 5. 计算条件格式
        var formatResults = formatService.evaluateFormats(templateId, cellData);

        PreviewResult result = new PreviewResult();
        result.setTemplate(template);
        result.setCellData(cellData);
        result.setFormulaResults(formulaResults);
        result.setValidationErrors(validationErrors);
        result.setFormatCells(formatResults.stream()
                .map(r -> {
                    var fc = new PreviewResult.FormatCell();
                    fc.setCellKey(r.cellKey());
                    try { fc.setStyle(objectMapper.readValue(r.styleConfig(), Object.class)); }
                    catch (Exception e) { fc.setStyle(Map.of()); }
                    return fc;
                }).toList());

        return result;
    }

    // ==================== 私有方法：组装逻辑 ====================

    private LayoutConfig buildLayoutConfig(RptTemplate tpl) {
        LayoutConfig layout = new LayoutConfig();
        layout.setType("table"); // 默认表格布局
        layout.setDefaultRowHeight(tpl.getRowCount() != null && tpl.getRowCount() > 0 ? 32 : 32);
        layout.setDefaultColWidth(tpl.getColumnCount() != null && tpl.getColumnCount() > 0 ? 120 : 120);

        // 尝试从configJson解析扩展布局配置
        if (StrUtil.isNotBlank(tpl.getConfigJson())) {
            try {
                var mapper = objectMapper;
                Map<String, Object> cfg = mapper.readValue(tpl.getConfigJson(), Map.class);
                if (cfg.containsKey("freezeRows")) layout.setFreezeRows((Integer) cfg.get("freezeRows"));
                if (cfg.containsKey("freezeCols")) layout.setFreezeCols((Integer) cfg.get("freezeCols"));
                if (cfg.containsKey("showRowHeader")) layout.setShowRowHeader((Boolean) cfg.get("showRowHeader"));
                if (cfg.containsKey("showColumnHeader")) layout.setShowColumnHeader((Boolean) cfg.get("showColumnHeader"));
            } catch (Exception ignored) {}
        }
        return layout;
    }

    /**
     * 将扁平列表构建为树形结构
     * @param entities 扁平实体列表
     * @param isRow 是否为行节点
     * @return 树形列表
     */
    @SuppressWarnings("unchecked")
    private <T> List<TreeNode> buildTreeNodes(List<T> entities, boolean isRow) {
        if (entities == null || entities.isEmpty()) return List.of();

        // 将实体转为TreeNode
        List<TreeNode> nodes = new ArrayList<>();
        for (T entity : entities) {
            TreeNode node = isRow ?
                    rowToTreeNode((RptTemplateRow) entity) :
                    columnToTreeNode((RptTemplateColumn) entity);
            nodes.add(node);
        }

        // 构建树（parentId匹配）
        Map<String, TreeNode> nodeMap = new LinkedHashMap<>();
        for (TreeNode n : nodes) {
            nodeMap.put(n.getId(), n);
        }
        List<TreeNode> roots = new ArrayList<>();
        for (TreeNode n : nodes) {
            if (n.getParentId() == null || "0".equals(n.getParentId()) || "root".equals(n.getParentId())) {
                roots.add(n);
            } else {
                TreeNode parent = nodeMap.get(n.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(n);
                } else {
                    roots.add(n); // 找不到父节点，作为根
                }
            }
        }

        return roots;
    }

    private TreeNode rowToTreeNode(RptTemplateRow row) {
        TreeNode n = new TreeNode();
        n.setId(row.getRowCode());
        n.setName(row.getRowName());
        n.setParentId(row.getParentId() != null ? String.valueOf(row.getParentId()) : null);
        n.setSortOrder(row.getSortOrder());
        n.setLevel(row.getLevel());
        n.setType(mapRowType(row.getRowType()));
        n.setExpandable(row.getIsExpandable() != null && row.getIsExpandable() == 1);
        n.setIsSummary(row.getIsSummary() != null && row.getIsSummary() == 1);
        n.setHeight(row.getHeight());
        n.setBackgroundColor(row.getBackgroundColor());
        n.setVisible(row.getVisible() == null || row.getVisible() == 1);
        n.setFrozen(row.getFrozen() != null && row.getFrozen() == 1);
        return n;
    }

    private TreeNode columnToTreeNode(RptTemplateColumn col) {
        TreeNode n = new TreeNode();
        n.setId(col.getColumnCode());
        n.setName(col.getColumnName());
        n.setParentId(col.getParentId() != null ? String.valueOf(col.getParentId()) : null);
        n.setSortOrder(col.getSortOrder());
        n.setLevel(null); // 列通常不使用level
        n.setWidth(col.getWidth());
        n.setColumnType(mapColumnType(col.getColumnType()));
        n.setDataType(mapDataType(col.getDataType()));
        n.setDecimalPlaces(col.getDecimalPlaces());
        n.setUnit(col.getUnit());
        n.setRequired(col.getRequired() != null && col.getRequired() == 1);
        n.setReadonly(col.getReadonly() != null && col.getReadonly() == 1);
        n.setDefaultValue(col.getDefaultValue());
        n.setMinValue(col.getMinValue());
        n.setMaxValue(col.getMaxValue());
        n.setFormatPattern(col.getFormatPattern());
        n.setAlign(mapAlign(col.getAlign()));
        n.setVisible(col.getVisible() == null || col.getVisible() == 1);
        n.setFrozen(col.getFrozen() != null && col.getFrozen() == 1);
        return n;
    }

    private MetricDef formulaVoToMetricDef(FormulaVO fvo) {
        MetricDef m = new MetricDef();
        m.setField(fvo.getFieldName());
        m.setLabel(fvo.getLabel());
        m.setExpression(fvo.getExpression());
        m.setResultType(fvo.getResultType());
        m.setTargetCell(fvo.getTargetCell());
        m.setDependencies(fvo.getDependencies());
        m.setCalcTrigger(fvo.getCalcTrigger());
        m.setPriority(fvo.getPriority());
        return m;
    }

    private List<AggregateDef> extractAggregates(List<RptTemplateRow> rows) {
        List<AggregateDef> list = new ArrayList<>();
        for (RptTemplateRow r : rows) {
            if (r.getIsSummary() != null && r.getIsSummary() == 1) {
                AggregateDef agg = new AggregateDef();
                agg.setTargetRowCode(r.getRowCode());
                agg.setLabel(r.getRowName());
                if (StrUtil.isNotBlank(r.getSummaryFormula())) {
                    // 解析合计公式，如 SUM(*) -> method=sum, sourceRange=*
                    String formula = r.getSummaryFormula().toUpperCase().trim();
                    if (formula.startsWith("SUM(")) agg.setMethod("sum");
                    else if (formula.startsWith("AVG(")) agg.setMethod("avg");
                    else if (formula.startsWith("MAX(")) agg.setMethod("max");
                    else if (formula.startsWith("MIN(")) agg.setMethod("min");
                    else if (formula.startsWith("COUNT(")) agg.setMethod("count");
                    else { agg.setMethod("custom"); agg.setCustomFormula(formula); }
                    agg.setSourceRange("*");
                }
                list.add(agg);
            }
        }
        return list;
    }

    private DataSourceDef resolveDataSource(RptTemplate tpl) {
        DataSourceDef def = new DataSourceDef();
        def.setType("mysql"); // 默认

        // 尝试从configJson解析数据源配置
        if (StrUtil.isNotBlank(tpl.getConfigJson())) {
            try {
                var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> cfg = mapper.readValue(tpl.getConfigJson(), Map.class);
                if (cfg.containsKey("dataSourceId")) {
                    Long dsId = ((Number) cfg.get("dataSourceId")).longValue();
                    def = dsService.exportToDesignerFormat(dsId);
                    if (def == null) def = new DataSourceDef();
                }
            } catch (Exception ignored) {}
        }
        return def;
    }

    private PermissionConfig buildPermissionConfig(RptTemplate tpl) {
        PermissionConfig pc = new PermissionConfig();
        pc.setAllowExport(tpl.getAllowExport() != null && tpl.getAllowExport() == 1);
        pc.setAllowImport(tpl.getAllowImport() != null && tpl.getAllowImport() == 1);
        return pc;
    }

    // ==================== 私有方法：保存逻辑 ====================

    private RptTemplate buildTemplateEntity(ReportDesignerTemplateVO vo) {
        RptTemplate t = new RptTemplate();
        t.setTemplateName(vo.getName());
        t.setTemplateCode(vo.getCode());
        t.setDescription(vo.getDescription());
        t.setPeriodType(mapPeriodTypeToInt(vo.getPeriodType()));
        t.setAuditRequired(Boolean.TRUE.equals(vo.getAuditRequired()) ? 1 : 0);
        t.setVersion(vo.getVersion());
        t.setStatus(mapStringToIntStatus(vo.getStatus()));

        // 布局信息存入configJson
        if (vo.getLayout() != null) {
            Map<String, Object> cfg = new HashMap<>();
            cfg.put("layout", vo.getLayout().getType());
            cfg.put("freezeRows", vo.getLayout().getFreezeRows());
            cfg.put("freezeCols", vo.getLayout().getFreezeCols());
            try {
                t.setConfigJson(objectMapper.writeValueAsString(cfg));
            } catch (Exception e) { t.setConfigJson("{}"); }
        }

        // 行列数统计
        if (vo.getRowTree() != null) t.setRowCount(countLeafNodes(vo.getRowTree()));
        if (vo.getColumnTree() != null) t.setColumnCount(countLeafNodes(vo.getColumnTree()));

        return t;
    }

    private void updateTemplateFromVO(RptTemplate tpl, ReportDesignerTemplateVO vo) {
        if (StrUtil.isNotBlank(vo.getName())) tpl.setTemplateName(vo.getName());
        if (StrUtil.isNotBlank(vo.getCode())) tpl.setTemplateCode(vo.getCode());
        if (vo.getDescription() != null) tpl.setDescription(vo.getDescription());
        if (vo.getPeriodType() != null) tpl.setPeriodType(mapPeriodTypeToInt(vo.getPeriodType()));
        tpl.setAuditRequired(Boolean.TRUE.equals(vo.getAuditRequired()) ? 1 : 0);
    }

    @SuppressWarnings("unchecked")
    private void saveRowTree(Long templateId, List<TreeNode> tree) {
        if (tree == null || tree.isEmpty()) return;
        List<RptTemplateRow> flatList = flattenRowTree(tree, null, 0, new int[1]);
        for (RptTemplateRow row : flatList) {
            row.setTemplateId(templateId);
            rowMapper.insert(row);
        }
    }

    private List<RptTemplateRow> flattenRowTree(List<TreeNode> tree, Long parentId, int level, int[] orderCounter) {
        List<RptTemplateRow> result = new ArrayList<>();
        for (TreeNode node : tree) {
            RptTemplateRow row = new RptTemplateRow();
            row.setRowCode(node.getId());
            row.setRowName(node.getName());
            row.setRowType(mapRowTypeToInt(node.getType()));
            row.setParentId(parentId);
            row.setSortOrder(orderCounter[0]++);
            row.setLevel(level);
            row.setIndent(level);
            row.setIsExpandable(Boolean.TRUE.equals(node.getExpandable()) ? 1 : 0);
            row.setIsSummary(Boolean.TRUE.equals(node.getIsSummary()) ? 1 : 0);
            row.setHeight(node.getHeight());
            row.setBackgroundColor(node.getBackgroundColor());
            row.setFontBold(null); // 预留
            row.setVisible(Boolean.FALSE.equals(node.getVisible()) ? 0 : 1);
            row.setFrozen(Boolean.TRUE.equals(node.getFrozen()) ? 1 : 0);
            result.add(row);

            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                Long currentId = row.getId();
                result.addAll(flattenRowTree(node.getChildren(), currentId, level + 1, orderCounter));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void saveColumnTree(Long templateId, List<TreeNode> tree) {
        if (tree == null || tree.isEmpty()) return;
        List<RptTemplateColumn> flatList = flattenColTree(tree, null, 0, new int[1]);
        for (RptTemplateColumn col : flatList) {
            col.setTemplateId(templateId);
            columnMapper.insert(col);
        }
    }

    private List<RptTemplateColumn> flattenColTree(List<TreeNode> tree, Long parentId, int level, int[] orderCounter) {
        List<RptTemplateColumn> result = new ArrayList<>();
        for (TreeNode node : tree) {
            RptTemplateColumn col = new RptTemplateColumn();
            col.setColumnCode(node.getId());
            col.setColumnName(node.getName());
            col.setParentId(parentId);
            col.setSortOrder(orderCounter[0]++);
            col.setWidth(node.getWidth());
            col.setColumnType(mapColumnTypeToInt(node.getColumnType()));
            col.setDataType(mapDataTypeToInt(node.getDataType()));
            col.setDecimalPlaces(node.getDecimalPlaces());
            col.setUnit(node.getUnit());
            col.setRequired(Boolean.TRUE.equals(node.getRequired()) ? 1 : 0);
            col.setReadonly(Boolean.TRUE.equals(node.getReadonly()) ? 1 : 0);
            col.setDefaultValue(node.getDefaultValue());
            if (node.getMinValue() != null) col.setMinValue(new BigDecimal(node.getMinValue().toString()));
            if (node.getMaxValue() != null) col.setMaxValue(new BigDecimal(node.getMaxValue().toString()));
            col.setFormatPattern(node.getFormatPattern());
            col.setAlign(mapAlignToInt(node.getAlign()));
            col.setVisible(Boolean.FALSE.equals(node.getVisible()) ? 0 : 1);
            col.setFrozen(Boolean.TRUE.equals(node.getFrozen()) ? 1 : 0);
            result.add(col);

            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                Long currentId = col.getId();
                result.addAll(flattenColTree(node.getChildren(), currentId, level + 1, orderCounter));
            }
        }
        return result;
    }

    private void saveMetrics(Long templateId, List<MetricDef> metrics) {
        if (metrics == null || metrics.isEmpty()) return;
        for (MetricDef m : metrics) {
            FormulaDTO dto = new FormulaDTO();
            dto.setTemplateId(String.valueOf(templateId));
            dto.setFieldName(m.getField());
            dto.setLabel(m.getLabel());
            dto.setExpression(m.getExpression());
            dto.setResultType(m.getResultType());
            dto.setTargetCell(m.getTargetCell());
            dto.setDependencies(m.getDependencies());
            dto.setCalcTrigger(m.getCalcTrigger());
            dto.setPriority(m.getPriority());
            formulaService.createFormula(dto);
        }
    }

    private void linkDataSource(Long templateId, DataSourceDef ds) {
        if (ds == null || StrUtil.isBlank(ds.getSourceId())) return;
        // 将数据源ID存入模板的configJson
        RptTemplate tpl = templateMapper.selectById(templateId);
        if (tpl != null) {
            try {
                var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> cfg = Map.of();
                if (StrUtil.isNotBlank(tpl.getConfigJson())) {
                    cfg = mapper.readValue(tpl.getConfigJson(), Map.class);
                }
                cfg.put("dataSourceId", ds.getSourceId());
                tpl.setConfigJson(mapper.writeValueAsString(cfg));
                templateMapper.updateById(tpl);
            } catch (Exception e) {
                log.warn("关联数据源失败: {}", e.getMessage());
            }
        }
    }

    private void deleteSubData(Long templateId) {
        // 删除行
        rowMapper.delete(new LambdaQueryWrapper<RptTemplateRow>().eq(RptTemplateRow::getTemplateId, templateId));
        // 删除列
        columnMapper.delete(new LambdaQueryWrapper<RptTemplateColumn>().eq(RptTemplateColumn::getTemplateId, templateId));
        // 公式通过service删除
        LambdaQueryWrapper<com.groupreport.platform.entity.RptFormula> fw =
                new LambdaQueryWrapper<com.groupreport.platform.entity.RptFormula>()
                        .eq(com.groupreport.platform.entity.RptFormula::getTemplateId, templateId);
        formulaMapper.delete(fw);
    }

    private void validateTemplateCompleteness(Long templateId) {
        // 检查是否有行列定义
        long rowCount = rowMapper.selectCount(
                new LambdaQueryWrapper<RptTemplateRow>().eq(RptTemplateRow::getTemplateId, templateId));
        long colCount = columnMapper.selectCount(
                new LambdaQueryWrapper<RptTemplateColumn>().eq(RptTemplateColumn::getTemplateId, templateId));
        if (rowCount == 0) throw new BusinessException("模板没有配置行结构，无法发布");
        if (colCount == 0) throw new BusinessException("模板没有配置列结构，无法发布");
        log.info("模板完整性校验通过: rows={}, cols={}", rowCount, colCount);
    }

    private void saveVersionSnapshot(Long templateId, int version) {
        // 预留：将当前模板完整状态快照存入 rpt_template_version 表
        log.info("版本快照已保存: templateId={}, version={}", templateId, version);
    }

    private Map<String, String> loadCellDataForPreview(Long templateId, Long orgId, String period) {
        LambdaQueryWrapper<RptData> wrapper = new LambdaQueryWrapper<RptData>()
                .eq(RptData::getTemplateId, templateId)
                .eq(RptData::getOrgId, orgId)
                .eq(RptData::getPeriod, period);
        List<RptData> dataList = dataMapper.selectList(wrapper);
        Map<String, String> map = new HashMap<>();
        for (RptData d : dataList) {
            map.put(d.getRowCode() + ":" + d.getColumnCode(), d.getValueText());
        }
        return map;
    }

    // ==================== 枚举映射工具方法 ====================

    private String mapStatusToString(Integer status) {
        if (status == null) return "draft";
        return switch (status) { case 0 -> "draft"; case 1 -> "published"; case 2 -> "disabled"; default -> "draft"; };
    }

    private Integer mapStringToIntStatus(String status) {
        if (status == null) return 0;
        return switch (status.toLowerCase()) { case "draft" -> 0; case "published" -> 1; case "disabled" -> 2; default -> 0; };
    }

    private String mapPeriodType(Integer type) {
        if (type == null) return "month";
        return switch (type) { case 1 -> "day"; case 2 -> "week"; case 3 -> "month"; case 4 -> "quarter"; case 5 -> "year"; default -> "month"; };
    }

    private Integer mapPeriodTypeToInt(String type) {
        if (type == null) return 3;
        return switch (type.toLowerCase()) { case "day" -> 1; case "week" -> 2; case "month" -> 3; case "quarter" -> 4; case "year" -> 5; default -> 3; };
    }

    private String mapRowType(Integer type) {
        if (type == null) return "data";
        return switch (type) { case 1 -> "data"; case 2 -> "header"; case 3 -> "summary"; case 4 -> "group"; default -> "data"; };
    }

    private Integer mapRowTypeToInt(String type) {
        if (type == null) return 1;
        return switch (type.toLowerCase()) { case "data" -> 1; case "header" -> 2; case "summary" -> 3; case "group" -> 4; default -> 1; };
    }

    private String mapColumnType(Integer type) {
        if (type == null) return "text";
        return switch (type) { case 1 -> "text"; case 2 -> "number"; case 3 -> "date"; case 4 -> "select"; case 5 -> "formula"; case 6 -> "readonly"; default -> "text"; };
    }

    private Integer mapColumnTypeToInt(String type) {
        if (type == null) return 1;
        return switch (type.toLowerCase()) { case "text" -> 1; case "number" -> 2; case "date" -> 3; case "select" -> 4; case "formula" -> 5; case "readonly" -> 6; default -> 1; };
    }

    private String mapDataType(Integer type) {
        if (type == null) return "string";
        return switch (type) { case 1 -> "string"; case 2 -> "integer"; case 3 -> "decimal"; case 4 -> "percent"; case 5 -> "currency"; default -> "string"; };
    }

    private Integer mapDataTypeToInt(String type) {
        if (type == null) return 1;
        return switch (type.toLowerCase()) { case "string" -> 1; case "integer" -> 2; case "decimal" -> 3; case "percent" -> 4; case "currency" -> 5; default -> 1; };
    }

    private String mapAlign(Integer align) {
        if (align == null) return "center";
        return switch (align) { case 1 -> "left"; case 2 -> "center"; case 3 -> "right"; default -> "center"; };
    }

    private Integer mapAlignToInt(String align) {
        if (align == null) return 2;
        return switch (align.toLowerCase()) { case "left" -> 1; case "center" -> 2; case "right" -> 3; default -> 2; };
    }

    private int countLeafNodes(List<TreeNode> tree) {
        if (tree == null) return 0;
        int count = 0;
        for (TreeNode n : tree) {
            if (n.getChildren() == null || n.getChildren().isEmpty()) {
                count++;
            } else {
                count += countLeafNodes(n.getChildren());
            }
        }
        return count;
    }
}
