package com.groupreport.platform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.groupreport.platform.common.ResultCode;
import com.groupreport.platform.dto.FormulaDTO;
import com.groupreport.platform.entity.RptData;
import com.groupreport.platform.entity.RptFormula;
import com.groupreport.platform.entity.RptTemplate;
import com.groupreport.platform.exception.BusinessException;
import com.groupreport.platform.mapper.RptDataMapper;
import com.groupreport.platform.mapper.RptFormulaMapper;
import com.groupreport.platform.mapper.RptTemplateMapper;
import com.groupreport.platform.service.ReportFormulaService;
import com.groupreport.platform.vo.FormulaVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 公式服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportFormulaServiceImpl extends ServiceImpl<RptFormulaMapper, RptFormula>
        implements ReportFormulaService {

    private final RptTemplateMapper templateMapper;
    private final RptDataMapper dataMapper;
    private final ObjectMapper objectMapper;

    /** 单元格引用正则：R{行}C{列} 或 Excel风格 A1:B10 */
    private static final Pattern CELL_REF_PATTERN = Pattern.compile("R(\\d+)C(\\d+)|([A-Z]+)(\\d+)");
    /** 范围引用正则：R1C1:R10C5 或 A1:B10 */
    private static final Pattern RANGE_PATTERN = Pattern.compile("(R\\d+C\\d+|[A-Z]+\\d+)\\s*:\\s*(R\\d+C\\d+|[A-Z]+\\d+)");

    @Override
    public List<FormulaVO> getFormulasByTemplateId(Long templateId) {
        // 校验模板存在
        checkTemplateExists(templateId);

        List<RptFormula> formulas = baseMapper.selectByTemplateId(templateId);
        return formulas.stream().map(this::toVO).toList();
    }

    @Override
    public FormulaVO getFormulaDetail(Long id) {
        RptFormula formula = baseMapper.selectById(id);
        if (formula == null) {
            throw new BusinessException(ResultCode.FORMULA_NOT_FOUND);
        }
        return toVO(formula);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFormula(FormulaDTO dto) {
        // 1. 解析模板ID（支持数字ID或模板编码）
        Long templateId = resolveTemplateId(dto.getTemplateId());
        checkTemplateExists(templateId);

        // 2. 解析目标单元格 "3-5" -> rowCode="3", columnCode="5"
        String[] target = parseTargetCell(dto.getTargetCell());
        String targetRowCode = target[0];
        String targetColumnCode = target[1];

        // 3. 检查目标单元格是否已有公式
        LambdaQueryWrapper<RptFormula> wrapper = new LambdaQueryWrapper<RptFormula>()
                .eq(RptFormula::getTemplateId, templateId)
                .eq(RptFormula::getTargetRowCode, targetRowCode)
                .eq(RptFormula::getTargetColumnCode, targetColumnCode)
                .eq(RptFormula::getStatus, 1);
        Long count = baseMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("该目标单元格已存在公式，请先删除或修改");
        }

        // 4. DTO -> Entity 转换（前端格式 → 数据库格式）
        RptFormula formula = convertToEntity(dto);
        formula.setTemplateId(templateId);
        formula.setTargetRowCode(targetRowCode);
        formula.setTargetColumnCode(targetColumnCode);

        baseMapper.insert(formula);
        log.info("创建公式: id={}, fieldName={}, label={}, target={}:{}",
                formula.getId(), dto.getFieldName(), dto.getLabel(), targetRowCode, targetColumnCode);

        return formula.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFormula(FormulaDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("公式ID不能为空");
        }

        RptFormula existing = baseMapper.selectById(dto.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.FORMULA_NOT_FOUND);
        }

        // DTO -> Entity 转换
        RptFormula updated = convertToEntity(dto);
        updated.setId(existing.getId());
        updated.setTemplateId(existing.getTemplateId());

        // 如果前端传了targetCell，重新解析
        if (StrUtil.isNotBlank(dto.getTargetCell())) {
            String[] target = parseTargetCell(dto.getTargetCell());
            updated.setTargetRowCode(target[0]);
            updated.setTargetColumnCode(target[1]);
        } else {
            updated.setTargetRowCode(existing.getTargetRowCode());
            updated.setTargetColumnCode(existing.getTargetColumnCode());
        }

        baseMapper.updateById(updated);
        log.info("更新公式: id={}, fieldName={}", existing.getId(), dto.getFieldName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFormula(Long id) {
        RptFormula formula = baseMapper.selectById(id);
        if (formula == null) {
            throw new BusinessException(ResultCode.FORMULA_NOT_FOUND);
        }
        baseMapper.deleteById(id);
        log.info("删除公式: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        RptFormula formula = baseMapper.selectById(id);
        if (formula == null) {
            throw new BusinessException(ResultCode.FORMULA_NOT_FOUND);
        }
        formula.setStatus(status);
        baseMapper.updateById(formula);
        log.info("更新公式状态: id={}, status={}", id, status);
    }

    /**
     * 执行指定模板的所有公式计算
     * 按优先级排序，依次执行，支持公式间依赖（低优先级可引用高优先级的计算结果）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> executeFormulas(Long templateId, Long orgId, String period, Integer calcTrigger) {
        List<RptFormula> formulas = baseMapper.selectByCalcTrigger(templateId, calcTrigger);
        if (formulas.isEmpty()) {
            return Map.of();
        }

        // 加载当前所有数据作为计算上下文
        Map<String, String> dataContext = loadDataContext(templateId, orgId, period);
        Map<String, String> results = new LinkedHashMap<>();

        for (RptFormula formula : formulas) {
            try {
                String value = doCalculate(formula, dataContext);
                String cellKey = formula.getTargetRowCode() + ":" + formula.getTargetColumnCode();
                results.put(cellKey, value);

                // 将结果写入上下文（供后续公式引用）
                dataContext.put(cellKey, value);

                // 保存计算结果到数据库
                saveCalcResult(templateId, orgId, period, formula, value);
            } catch (Exception e) {
                log.error("公式计算失败: id={}, expression={}, error={}",
                        formula.getId(), formula.getFormulaExpression(), e.getMessage());
                // 继续执行其他公式，不中断整体流程
            }
        }

        log.info("公式批量计算完成: templateId={}, orgId={}, period={}, count={}",
                templateId, orgId, period, results.size());

        return results;
    }

    @Override
    public String executeSingleFormula(Long formulaId, Long orgId, String period) {
        RptFormula formula = baseMapper.selectById(formulaId);
        if (formula == null) {
            throw new BusinessException(ResultCode.FORMULA_NOT_FOUND);
        }

        Map<String, String> dataContext = loadDataContext(formula.getTemplateId(), orgId, period);
        String result = doCalculate(formula, dataContext);

        // 保存结果
        saveCalcResult(formula.getTemplateId(), orgId, period, formula, result);

        return result;
    }

    // ==================== 私有方法 ====================

    /**
     * 加载数据上下文（模板下某组织某周期的所有数据）
     */
    private Map<String, String> loadDataContext(Long templateId, Long orgId, String period) {
        LambdaQueryWrapper<RptData> wrapper = new LambdaQueryWrapper<RptData>()
                .eq(RptData::getTemplateId, templateId)
                .eq(RptData::getOrgId, orgId)
                .eq(RptData::getPeriod, period);
        List<RptData> dataList = dataMapper.selectList(wrapper);

        Map<String, String> context = new HashMap<>();
        for (RptData data : dataList) {
            String key = data.getRowCode() + ":" + data.getColumnCode();
            context.put(key, data.getValueText());
        }
        return context;
    }

    /**
     * 核心计算逻辑
     * 支持内置函数：SUM, AVG, MAX, MIN, COUNT
     * 支持单元格引用：R1C1, R2C3 等格式
     * 支持范围引用：R1C1:R10C5
     * 支持四则运算表达式
     */
    private String doCalculate(RptFormula formula, Map<String, String> dataContext) {
        String expression = formula.getFormulaExpression().trim();
        int type = formula.getFormulaType();

        // 根据公式类型选择计算策略
        return switch (type) {
            case 1 -> calculateSum(expression, dataContext);       // 求和
            case 2 -> calculateAvg(expression, dataContext);       // 平均值
            case 3 -> calculateMax(expression, dataContext);       // 最大值
            case 4 -> calculateMin(expression, dataContext);       // 最小值
            case 5 -> calculateCustom(expression, dataContext);    // 自定义表达式
            default -> throw new BusinessException("不支持的公式类型: " + type);
        };
    }

    /**
     * 求和计算
     * SUM(R1C1:R10C5) 或 SUM(R1C1,R1C2,R1C3)
     */
    private String calculateSum(String expression, Map<String, String> dataContext) {
        List<BigDecimal> values = extractValuesFromExpression(expression, dataContext);
        BigDecimal sum = values.stream().filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    /**
     * 平均值计算
     */
    private String calculateAvg(String expression, Map<String, String> dataContext) {
        List<BigDecimal> values = extractValuesFromExpression(expression, dataContext);
        List<BigDecimal> validValues = values.stream().filter(Objects::nonNull).toList();
        if (validValues.isEmpty()) {
            return "0";
        }
        BigDecimal sum = validValues.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(validValues.size()), 4, RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString();
    }

    /**
     * 最大值计算
     */
    private String calculateMax(String expression, Map<String, String> dataContext) {
        List<BigDecimal> values = extractValuesFromExpression(expression, dataContext);
        Optional<BigDecimal> max = values.stream()
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder());
        return max.map(m -> m.stripTrailingZeros().toPlainString()).orElse(null);
    }

    /**
     * 最小值计算
     */
    private String calculateMin(String expression, Map<String, String> dataContext) {
        List<BigDecimal> values = extractValuesFromExpression(expression, dataContext);
        Optional<BigDecimal> min = values.stream()
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder());
        return min.map(m -> m.stripTrailingZeros().toPlainString()).orElse(null);
    }

    /**
     * 自定义表达式计算
     * 解析四则运算 + 单元格引用
     * 例如：R1C1+R1C2*0.5-R1C3/100
     */
    private String calculateCustom(String expression, Map<String, String> dataContext) {
        // 先替换所有单元格引用为实际值
        String resolvedExpr = resolveCellReferences(expression, dataContext);

        try {
            // 安全的数学表达式求值（仅允许数字和运算符）
            double result = evaluateMathExpression(resolvedExpr);
            return BigDecimal.valueOf(result).setScale(4, RoundingMode.HALF_UP)
                    .stripTrailingZeros().toPlainString();
        } catch (Exception e) {
            log.warn("自定义表达式计算失败: expression={}, error={}", resolvedExpr, e.getMessage());
            throw new BusinessException("公式表达式计算失败: " + e.getMessage());
        }
    }

    /**
     * 从表达式中提取数值列表
     * 支持：SUM(R1C1:R10C5) 范围语法 和 SUM(A,B,C) 列表语法
     */
    private List<BigDecimal> extractValuesFromExpression(String expression, Map<String, String> dataContext) {
        List<BigDecimal> values = new ArrayList<>();

        // 尝试匹配范围引用
        Matcher rangeMatcher = RANGE_PATTERN.matcher(expression);
        while (rangeMatcher.find()) {
            String rangeStr = rangeMatcher.group();
            List<String> cells = expandRange(rangeStr);
            for (String cell : cells) {
                String val = dataContext.get(cell);
                if (StrUtil.isNotBlank(val)) {
                    try {
                        values.add(new BigDecimal(val));
                    } catch (NumberFormatException ignored) {
                        // 非数值忽略
                    }
                }
            }
        }

        // 如果没有范围引用，尝试提取单个单元格
        if (values.isEmpty()) {
            Matcher cellMatcher = CELL_REF_PATTERN.matcher(expression);
            while (cellMatcher.find()) {
                String cellRef = cellMatcher.group();
                // 转换为 rowCode:columnCode 格式
                String cellKey = normalizeCellRef(cellRef);
                String val = dataContext.get(cellKey);
                if (StrUtil.isNotBlank(val)) {
                    try {
                        values.add(new BigDecimal(val));
                    } catch (NumberFormatException ignored) {
                        // 非数值忽略
                    }
                }
            }
        }

        return values;
    }

    /**
     * 展开范围引用为单元格列表
     * R1C1:R3C2 -> [R1C1, R1C2, R2C1, R2C2, R3C1, R3C2]
     */
    private List<String> expandRange(String rangeStr) {
        List<String> cells = new ArrayList<>();
        String[] parts = rangeStr.split("\\s*:\\s*");
        if (parts.length != 2) {
            return cells;
        }

        int[] start = parseCellPosition(parts[0]);
        int[] end = parseCellPosition(parts[1]);

        if (start == null || end == null) {
            return cells;
        }

        int minRow = Math.min(start[0], end[0]);
        int maxRow = Math.max(start[0], end[0]);
        int minCol = Math.min(start[1], end[1]);
        int maxCol = Math.max(start[1], end[1]);

        for (int r = minRow; r <= maxRow; r++) {
            for (int c = minCol; c <= maxCol; c++) {
                cells.add("R" + r + "C" + c);
            }
        }
        return cells;
    }

    /**
     * 解析单元格位置 [rowIndex, colIndex]
     */
    private int[] parseCellPosition(String cellRef) {
        Matcher m = Pattern.compile("R(\\d+)C(\\d+)").matcher(cellRef.trim());
        if (m.matches()) {
            return new int[]{Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))};
        }
        m = Pattern.compile("([A-Z]+)(\\d+)").matcher(cellRef.trim());
        if (m.matches()) {
            String colLetters = m.group(1);
            int col = 0;
            for (char c : colLetters.toCharArray()) {
                col = col * 26 + (c - 'A' + 1);
            }
            return new int[]{Integer.parseInt(m.group(2)), col};
        }
        return null;
    }

    /**
     * 规范化单元格引用为 rowCode:columnCode 格式
     */
    private String normalizeCellRef(String cellRef) {
        int[] pos = parseCellPosition(cellRef);
        if (pos != null) {
            return pos[0] + ":" + pos[1];
        }
        return cellRef;
    }

    /**
     * 替换表达式中的单元格引用为实际值
     */
    private String resolveCellReferences(String expression, Map<String, String> dataContext) {
        StringBuilder result = new StringBuilder(expression);
        Matcher matcher = CELL_REF_PATTERN.matcher(expression);

        // 收集所有匹配位置
        int[][] matches = new int[16][2];
        int matchCount = 0;
        while (matcher.find()) {
            if (matchCount >= matches.length) {
                int[][] expanded = new int[matches.length * 2][2];
                System.arraycopy(matches, 0, expanded, 0, matches.length);
                matches = expanded;
            }
            matches[matchCount][0] = matcher.start();
            matches[matchCount][1] = matcher.end();
            matchCount++;
        }

        // 从后往前替换，避免位置偏移问题
        for (int i = matchCount - 1; i >= 0; i--) {
            int start = matches[i][0];
            int end = matches[i][1];
            String cellRef = expression.substring(start, end);
            String cellKey = normalizeCellRef(cellRef);
            String value = dataContext.getOrDefault(cellKey, "0");
            result.replace(start, end, value);
        }

        return result.toString();
    }

    /**
     * 安全数学表达式求值（仅支持数字、+-*
     */
    private double evaluateMathExpression(String expr) {
        // 清理表达式，只保留合法字符
        String cleaned = expr.replaceAll("[^0-9.\\+\\-\\*\\/\\(\\)]", "");
        if (cleaned.isBlank()) {
            return 0.0;
        }

        // 使用 ScriptEngine 的简化实现 — 手动解析四则运算
        return parseAndEvaluate(cleaned);
    }

    /**
     * 递归解析并计算四则运算表达式
     */
    private double parseAndEvaluate(String expr) {
        expr = expr.trim();

        // 处理括号
        int depth = 0;
        int lastParenEnd = -1;
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (c == '(') {
                depth++;
                if (depth == 1 && lastParenEnd < i - 1) {
                    // 括号前有内容，先处理前面的加减法
                }
            } else if (c == ')') {
                depth--;
                if (depth == 0) {
                    lastParenEnd = i;
                }
            }
        }

        // 如果最外层被括号包裹，去掉外层括号
        if (expr.startsWith("(") && findMatchingParen(expr, 0) == expr.length() - 1) {
            expr = expr.substring(1, expr.length() - 1).trim();
        }

        // 按优先级解析：先处理 +- （从右往左），再处理 */（从左往右）
        // 这里用递归下降方式处理

        return parseAddSub(expr);
    }

    private double parseAddSub(String expr) {
        int depth = 0;
        // 从右往左找最低优先级的 + 或 -
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            if (c == ')') depth++;
            else if (c == '(') depth--;
            else if (depth == 0 && (c == '+' || c == '-')) {
                // 排除负号开头的情况
                if (i > 0 && Character.isDigit(expr.charAt(i - 1))) {
                    double left = parseAddSub(expr.substring(0, i));
                    double right = parseMulDiv(expr.substring(i + 1));
                    return c == '+' ? left + right : left - right;
                } else if (i > 0 && expr.charAt(i - 1) == ')') {
                    double left = parseAddSub(expr.substring(0, i));
                    double right = parseMulDiv(expr.substring(i + 1));
                    return c == '+' ? left + right : left - right;
                }
            }
        }
        return parseMulDiv(expr);
    }

    private double parseMulDiv(String expr) {
        int depth = 0;
        // 从左往找 * 或 /
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') depth--;
            else if (depth == 0 && (c == '*' || c == '/')) {
                double left = parseMulDiv(expr.substring(0, i));
                double right = parsePrimary(expr.substring(i + 1));
                return c == '*' ? left * right : left / right;
            }
        }
        return parsePrimary(expr);
    }

    private double parsePrimary(String expr) {
        expr = expr.trim();
        if (expr.startsWith("(")) {
            int end = findMatchingParen(expr, 0);
            return parseAddSub(expr.substring(1, end));
        }
        try {
            return Double.parseDouble(expr);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int findMatchingParen(String expr, int openPos) {
        int depth = 0;
        for (int i = openPos; i < expr.length(); i++) {
            if (expr.charAt(i) == '(') depth++;
            else if (expr.charAt(i) == ')') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return expr.length() - 1;
    }

    /**
     * 保存计算结果到数据库
     */
    private void saveCalcResult(Long templateId, Long orgId, String period,
                                RptFormula formula, String value) {
        // 查询是否已存在记录
        LambdaQueryWrapper<RptData> wrapper = new LambdaQueryWrapper<RptData>()
                .eq(RptData::getTemplateId, templateId)
                .eq(RptData::getOrgId, orgId)
                .eq(RptData::getPeriod, period)
                .eq(RptData::getRowCode, formula.getTargetRowCode())
                .eq(RptData::getColumnCode, formula.getTargetColumnCode());

        RptData existing = dataMapper.selectOne(wrapper);
        if (existing != null) {
            existing.setValueText(value);
            existing.setIsFormula(1);
            dataMapper.updateById(existing);
        } else {
            RptData data = new RptData();
            data.setTemplateId(templateId);
            data.setOrgId(orgId);
            data.setPeriod(period);
            data.setRowCode(formula.getTargetRowCode());
            data.setColumnCode(formula.getTargetColumnCode());
            data.setValueText(value);
            data.setIsFormula(1);
            dataMapper.insert(data);
        }
    }

    /**
     * 实体转VO（数据库格式 → 前端Univer格式）
     */
    private FormulaVO toVO(RptFormula formula) {
        FormulaVO vo = new FormulaVO();

        // 基础字段
        vo.setId(formula.getId());
        vo.setTemplateId(String.valueOf(formula.getTemplateId()));

        // 核心字段（对齐前端）
        vo.setFieldName(formula.getFormulaName());
        vo.setLabel(formula.getDescription());  // description 存储显示标签
        vo.setExpression(formula.getFormulaExpression());
        vo.setTargetCell(formula.getTargetRowCode() + "-" + formula.getTargetColumnCode());

        // resultType 从 formulaType 反向映射
        vo.setResultType(mapFormulaTypeToResultType(formula.getFormulaType()));

        // dependencies 从 sourceRange 反序列化
        if (StrUtil.isNotBlank(formula.getSourceRange())) {
            try {
                List<String> deps = objectMapper.readValue(formula.getSourceRange(), List.class);
                vo.setDependencies(deps);
            } catch (JsonProcessingException e) {
                // 如果不是JSON数组，按逗号分割作为fallback
                vo.setDependencies(Arrays.asList(formula.getSourceRange().split(",")));
            }
        }

        // 后端扩展字段
        vo.setTargetRowCode(formula.getTargetRowCode());
        vo.setTargetColumnCode(formula.getTargetColumnCode());
        vo.setSourceRange(formula.getSourceRange());
        vo.setCalcTrigger(mapCalcTriggerToString(formula.getCalcTrigger()));
        vo.setPriority(formula.getPriority());
        vo.setDescription(formula.getDescription());
        vo.setStatus(formula.getStatus());

        // 时间戳（对齐前端命名）
        vo.setCreatedAt(formula.getCreateTime());
        vo.setUpdatedAt(formula.getUpdateTime());

        // 查询模板名称
        if (formula.getTemplateId() != null) {
            RptTemplate tpl = templateMapper.selectById(formula.getTemplateId());
            if (tpl != null) {
                vo.setTemplateName(tpl.getTemplateName());
            }
        }

        return vo;
    }

    // ==================== DTO -> Entity 转换器 ====================

    /**
     * 前端DTO → 数据库实体 转换
     */
    private RptFormula convertToEntity(FormulaDTO dto) {
        RptFormula formula = new RptFormula();

        formula.setFormulaName(dto.getFieldName());
        formula.setFormulaExpression(dto.getExpression());
        formula.setDescription(dto.getLabel());  // label存入description字段

        // resultType -> formulaType 映射
        formula.setFormulaType(mapResultTypeToFormulaType(dto.getResultType()));

        // calcTrigger 字符串 -> 数字
        formula.setCalcTrigger(mapCalcTriggerToInt(dto.getCalcTrigger()));

        // dependencies 列表 -> sourceRange JSON存储
        if (dto.getDependencies() != null && !dto.getDependencies().isEmpty()) {
            try {
                formula.setSourceRange(objectMapper.writeValueAsString(dto.getDependencies()));
            } catch (JsonProcessingException e) {
                formula.setSourceRange(String.join(",", dto.getDependencies()));
            }
        }

        formula.setPriority(dto.getPriority());
        formula.setStatus(dto.getStatus());

        return formula;
    }

    /**
     * 解析模板ID：支持数字ID或模板编码字符串
     * "1" 或 1 -> Long(1)
     * "tpl_monthly_202601" -> 查询得到Long ID
     */
    private Long resolveTemplateId(String templateIdStr) {
        if (StrUtil.isBlank(templateIdStr)) {
            throw new BusinessException("模板ID不能为空");
        }
        // 尝试直接解析为数字ID
        try {
            return Long.parseLong(templateIdStr);
        } catch (NumberFormatException e) {
            // 作为模板编码查询
            LambdaQueryWrapper<RptTemplate> wrapper = new LambdaQueryWrapper<RptTemplate>()
                    .eq(RptTemplate::getTemplateCode, templateIdStr)
                    .last("LIMIT 1");
            RptTemplate tpl = templateMapper.selectOne(wrapper);
            if (tpl == null) {
                throw new BusinessException("模板不存在: " + templateIdStr);
            }
            return tpl.getId();
        }
    }

    /**
     * 解析目标单元格 "3-5" -> ["3", "5"]
     * 支持多种分隔符：- : /
     */
    private String[] parseTargetCell(String targetCell) {
        if (StrUtil.isBlank(targetCell)) {
            throw new BusinessException("目标单元格不能为空");
        }
        String[] parts = targetCell.split("[-:/]");
        if (parts.length != 2 || StrUtil.hasBlank(parts)) {
            throw new BusinessException("目标单元格格式错误，应为 行号-列号，如：3-5");
        }
        return parts;
    }

    // ==================== 枚举映射方法 ====================

    /**
     * 前端resultType -> 数据库formulaType
     */
    private int mapResultTypeToFormulaType(String resultType) {
        if (StrUtil.isBlank(resultType)) return 5; // 默认自定义表达式
        return switch (resultType.toLowerCase()) {
            case "number", "sum", "求和" -> 1;
            case "avg", "average", "平均值" -> 2;
            case "max", "最大值" -> 3;
            case "min", "最小值" -> 4;
            default -> 5; // percent, currency, string 等走自定义表达式
        };
    }

    /**
     * 数据库formulaType -> 前端resultType
     */
    private String mapFormulaTypeToResultType(Integer formulaType) {
        if (formulaType == null) return "number";
        return switch (formulaType) {
            case 1 -> "sum";
            case 2 -> "avg";
            case 3 -> "max";
            case 4 -> "min";
            default -> "custom";
        };
    }

    /**
     * 前端calcTrigger(字符串) -> 数据库calcTrigger(数字)
     */
    private int mapCalcTriggerToInt(String calcTrigger) {
        if (StrUtil.isBlank(calcTrigger)) return 2; // 默认保存时
        return switch (calcTrigger.toLowerCase()) {
            case "realtime", "实时", "1" -> 1;
            case "save", "保存时", "2" -> 2;
            case "submit", "提交时", "3" -> 3;
            default -> 2;
        };
    }

    /**
     * 数据库calcTrigger(数字) -> 前端calcTrigger(字符串)
     */
    private String mapCalcTriggerToString(Integer calcTrigger) {
        if (calcTrigger == null) return "save";
        return switch (calcTrigger) {
            case 1 -> "realtime";
            case 2 -> "save";
            case 3 -> "submit";
            default -> "save";
        };
    }

    private void checkTemplateExists(Long templateId) {
        RptTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }
    }
}
