package com.groupreport.platform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.groupreport.platform.common.ResultCode;
import com.groupreport.platform.dto.ConditionalFormatDTO;
import com.groupreport.platform.entity.RptConditionalFormat;
import com.groupreport.platform.entity.RptTemplate;
import com.groupreport.platform.exception.BusinessException;
import com.groupreport.platform.mapper.RptConditionalFormatMapper;
import com.groupreport.platform.mapper.RptTemplateMapper;
import com.groupreport.platform.service.ReportConditionalFormatService;
import com.groupreport.platform.vo.ConditionalFormatVO;
import com.groupreport.platform.vo.ReportDesignerTemplateVO.ConditionalFormatDef;
import com.groupreport.platform.vo.ReportDesignerTemplateVO.ConditionalFormatDef.StyleConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 条件格式服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportConditionalFormatServiceImpl extends ServiceImpl<RptConditionalFormatMapper, RptConditionalFormat>
        implements ReportConditionalFormatService {

    private final RptTemplateMapper templateMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<ConditionalFormatVO> getFormatsByTemplateId(Long templateId) {
        checkTemplateExists(templateId);
        List<RptConditionalFormat> list = baseMapper.selectByTemplateId(templateId);
        return list.stream().map(this::toVO).toList();
    }

    @Override
    public ConditionalFormatVO getFormatDetail(Long id) {
        RptConditionalFormat f = baseMapper.selectById(id);
        if (f == null) throw new BusinessException("条件格式规则不存在");
        return toVO(f);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFormat(ConditionalFormatDTO dto) {
        checkTemplateExists(dto.getTemplateId());
        RptConditionalFormat format = convertToEntity(dto);
        baseMapper.insert(format);
        log.info("创建条件格式: id={}, name={}, conditionType={}", format.getId(), dto.getName(), dto.getConditionType());
        return format.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFormat(ConditionalFormatDTO dto) {
        if (dto.getId() == null) throw new BusinessException("规则ID不能为空");
        RptConditionalFormat existing = baseMapper.selectById(dto.getId());
        if (existing == null) throw new BusinessException("条件格式规则不存在");
        RptConditionalFormat updated = convertToEntity(dto);
        updated.setId(existing.getId());
        updated.setTemplateId(existing.getTemplateId());
        baseMapper.updateById(updated);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFormat(Long id) {
        baseMapper.deleteById(id);
    }

    /**
     * 核心方法：根据条件格式规则评估数据，返回需要应用样式的单元格列表
     */
    @Override
    public List<ConditionalFormatResult> evaluateFormats(Long templateId, Map<String, String> cellData) {
        List<RptConditionalFormat> formats = baseMapper.selectByTemplateId(templateId);
        List<ConditionalFormatResult> results = new ArrayList<>();

        for (RptConditionalFormat f : formats) {
            // 解析应用范围
            Set<String> rangeCells = expandRange(f.getApplyRange());

            for (String cellKey : rangeCells) {
                String value = cellData.get(cellKey);
                if (checkCondition(f, value)) {
                    results.add(new ConditionalFormatResult(cellKey, f.getStyleConfig()));
                    if (Boolean.TRUE.equals(f.getStopIfTrue())) break; // 停止后续规则
                }
            }
        }

        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importFromDesignerJson(Long templateId, List<ConditionalFormatDef> formats) {
        LambdaQueryWrapper<RptConditionalFormat> delWrapper = new LambdaQueryWrapper<RptConditionalFormat>()
                .eq(RptConditionalFormat::getTemplateId, templateId);
        baseMapper.delete(delWrapper);

        if (formats == null || formats.isEmpty()) return;

        int sortOrder = 0;
        for (ConditionalFormatDef def : formats) {
            RptConditionalFormat entity = new RptConditionalFormat();
            entity.setTemplateId(templateId);
            entity.setFormatName(def.getName());
            entity.setConditionType(mapCondTypeToInt(def.getConditionType()));
            entity.setOperator(def.getOperator());
            try { entity.setConditionValue(objectMapper.writeValueAsString(def.getValue())); }
            catch (JsonProcessingException e) { entity.setConditionValue(null); }
            try { entity.setConditionValue2(objectMapper.writeValueAsString(def.getValue2())); }
            catch (JsonProcessingException e) { entity.setConditionValue2(null); }
            entity.setApplyRange(def.getApplyRange());
            if (def.getStyle() != null) {
                try { entity.setStyleConfig(objectMapper.writeValueAsString(def.getStyle())); }
                catch (JsonProcessingException e) { entity.setStyleConfig("{}"); }
            }
            entity.setStopIfTrue(Boolean.TRUE.equals(def.getStopIfTrue()));
            entity.setSortOrder(sortOrder++);
            entity.setStatus(1);
            baseMapper.insert(entity);
        }
        log.info("从设计器导入条件格式: templateId={}, count={}", templateId, formats.size());
    }

    @Override
    public List<ConditionalFormatDef> exportToDesignerJson(Long templateId) {
        List<RptConditionalFormat> list = baseMapper.selectByTemplateId(templateId);
        List<ConditionalFormatDef> result = new ArrayList<>();
        for (RptConditionalFormat f : list) {
            ConditionalFormatDef def = new ConditionalFormatDef();
            def.setName(f.getFormatName());
            def.setConditionType(mapIntToCondType(f.getConditionType()));
            def.setOperator(f.getOperator());
            try { def.setValue(objectMapper.readValue(f.getConditionValue(), Object.class)); }
            catch (Exception e) {}
            try { def.setValue2(objectMapper.readValue(f.getConditionValue2(), Object.class)); }
            catch (Exception e) {}
            def.setApplyRange(f.getApplyRange());
            try { def.setStyle(objectMapper.readValue(f.getStyleConfig(), StyleConfig.class)); }
            catch (Exception e) { def.setStyle(null); }
            def.setStopIfTrue(f.getStopIfTrue());
            result.add(def);
        }
        return result;
    }

    // ==================== 私有方法 ====================

    private boolean checkCondition(RptConditionalFormat f, String value) {
        if (StrUtil.isBlank(value)) {
            // 空值特殊处理
            return f.getConditionType() == 5; // blank类型匹配空值
        }

        String op = f.getOperator();
        if (op == null) return false;

        try {
            BigDecimal numVal = new BigDecimal(value.trim().replace(",", ""));
            String condValStr = f.getConditionValue();
            if (StrUtil.isBlank(condValStr)) return false;

            Object condValParsed = objectMapper.readValue(condValStr, Object.class);
            BigDecimal condNum = condValParsed instanceof Number ?
                    BigDecimal.valueOf(((Number) condValParsed).doubleValue()) :
                    new BigDecimal(condValParsed.toString());

            return switch (op.toLowerCase()) {
                case "eq", "=" -> numVal.compareTo(condNum) == 0;
                case "ne", "<>", "!=" -> numVal.compareTo(condNum) != 0;
                case "gt", ">" -> numVal.compareTo(condNum) > 0;
                case "ge", ">=" -> numVal.compareTo(condNum) >= 0;
                case "lt", "<" -> numVal.compareTo(condNum) < 0;
                case "le", "<=" -> numVal.compareTo(condNum) <= 0;
                case "contains" -> value.contains(condValParsed.toString());
                case "between" -> {
                    String val2Str = f.getConditionValue2();
                    if (val2Str != null) {
                        Object v2 = objectMapper.readValue(val2Str, Object.class);
                        BigDecimal condNum2 = v2 instanceof Number ?
                                BigDecimal.valueOf(((Number) v2).doubleValue()) :
                                new BigDecimal(v2.toString());
                        yield numVal.compareTo(condNum) >= 0 && numVal.compareTo(condNum2) <= 0;
                    }
                    yield false;
                }
                default -> false;
            };
        } catch (Exception e) {
            // 非数值比较，退化为字符串包含
            if ("contains".equals(op)) {
                try {
                    String cv = objectMapper.readValue(f.getConditionValue(), String.class);
                    return value.contains(cv);
                } catch (Exception ex) {
                    return false;
                }
            }
            return false;
        }
    }

    private Set<String> expandRange(String applyRange) {
        Set<String> cells = new HashSet<>();
        if (StrUtil.isBlank(applyRange)) return cells;

        // 支持逗号分隔多个范围
        String[] ranges = applyRange.split(",");
        for (String range : ranges) {
            range = range.trim();
            if (range.contains(":") && !range.startsWith("R")) {
                // 范围引用 R1C1:R10C5
                String[] parts = range.split("\\s*:\\s*");
                if (parts.length == 2) {
                    cells.addAll(expandCellRange(parts[0].trim(), parts[1].trim()));
                }
            } else {
                cells.add(range);
            }
        }
        return cells;
    }

    private Set<String> expandCellRange(String start, String end) {
        Set<String> result = new HashSet<>();
        int[] startPos = parsePosition(start);
        int[] endPos = parsePosition(end);
        if (startPos == null || endPos == null) return result;

        int minR = Math.min(startPos[0], endPos[0]);
        int maxR = Math.max(startPos[0], endPos[0]);
        int minC = Math.min(startPos[1], endPos[1]);
        int maxC = Math.max(startPos[1], endPos[1]);

        for (int r = minR; r <= maxR; r++) {
            for (int c = minC; c <= maxC; c++) {
                result.add(r + ":" + c);
            }
        }
        return result;
    }

    private int[] parsePosition(String pos) {
        pos = pos.trim();
        var m = java.util.regex.Pattern.compile("R?(\\d+)C?(\\d+)?").matcher(pos);
        if (m.find()) {
            int r = Integer.parseInt(m.group(1));
            int c = m.group(2) != null ? Integer.parseInt(m.group(2)) : 0;
            return new int[]{r, c};
        }
        return null;
    }

    private RptConditionalFormat convertToEntity(ConditionalFormatDTO dto) {
        RptConditionalFormat f = new RptConditionalFormat();
        f.setTemplateId(dto.getTemplateId());
        f.setFormatName(dto.getName());
        f.setConditionType(mapCondTypeToInt(dto.getConditionType()));
        f.setOperator(dto.getOperator());
        try { f.setConditionValue(objectMapper.writeValueAsString(dto.getValue())); }
        catch (JsonProcessingException e) { f.setConditionValue(null); }
        try { f.setConditionValue2(objectMapper.writeValueAsString(dto.getValue2())); }
        catch (JsonProcessingException e) { f.setConditionValue2(null); }
        f.setApplyRange(dto.getApplyRange());
        if (dto.getStyle() != null) {
            try { f.setStyleConfig(objectMapper.writeValueAsString(dto.getStyle())); }
            catch (JsonProcessingException e) { f.setStyleConfig("{}"); }
        }
        f.setStopIfTrue(dto.getStopIfTrue());
        f.setSortOrder(dto.getSortOrder());
        f.setStatus(dto.getStatus());
        return f;
    }

    private ConditionalFormatVO toVO(RptConditionalFormat f) {
        ConditionalFormatVO vo = new ConditionalFormatVO();
        vo.setId(f.getId());
        vo.setTemplateId(f.getTemplateId());
        vo.setName(f.getFormatName());
        vo.setConditionType(mapIntToCondType(f.getConditionType()));
        vo.setOperator(f.getOperator());
        try { vo.setValue(objectMapper.readValue(f.getConditionValue(), Object.class)); }
        catch (Exception e) {}
        try { vo.setValue2(objectMapper.readValue(f.getConditionValue2(), Object.class)); }
        catch (Exception e) {}
        vo.setApplyRange(f.getApplyRange());
        try { vo.setStyle(objectMapper.readValue(f.getStyleConfig(), Map.class)); }
        catch (Exception e) { vo.setStyle(Map.of()); }
        vo.setStopIfTrue(f.getStopIfTrue());
        vo.setSortOrder(f.getSortOrder());
        vo.setStatus(f.getStatus());
        vo.setCreatedAt(f.getCreateTime());
        vo.setUpdatedAt(f.getUpdateTime());
        return vo;
    }

    private int mapCondTypeToInt(String type) {
        if (type == null) return 1;
        return switch (type.toLowerCase()) {
            case "cell_value" -> 1;
            case "formula" -> 2;
            case "top_bottom" -> 3;
            case "duplicate" -> 4;
            case "blank" -> 5;
            default -> 1;
        };
    }

    private String mapIntToCondType(Integer type) {
        if (type == null) return "cell_value";
        return switch (type) {
            case 1 -> "cell_value";
            case 2 -> "formula";
            case 3 -> "top_bottom";
            case 4 -> "duplicate";
            case 5 -> "blank";
            default -> "cell_value";
        };
    }

    private void checkTemplateExists(Long templateId) {
        RptTemplate t = templateMapper.selectById(templateId);
        if (t == null) throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
    }
}
