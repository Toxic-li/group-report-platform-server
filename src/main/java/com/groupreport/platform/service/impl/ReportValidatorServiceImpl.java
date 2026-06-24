package com.groupreport.platform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.groupreport.platform.common.ResultCode;
import com.groupreport.platform.dto.ValidatorDTO;
import com.groupreport.platform.entity.RptTemplate;
import com.groupreport.platform.entity.RptValidator;
import com.groupreport.platform.exception.BusinessException;
import com.groupreport.platform.mapper.RptTemplateMapper;
import com.groupreport.platform.mapper.RptValidatorMapper;
import com.groupreport.platform.service.ReportValidatorService;
import com.groupreport.platform.vo.ReportDesignerTemplateVO.ValidatorDef;
import com.groupreport.platform.vo.ValidatorVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 校验规则服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportValidatorServiceImpl extends ServiceImpl<RptValidatorMapper, RptValidator>
        implements ReportValidatorService {

    private final RptTemplateMapper templateMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<ValidatorVO> getValidatorsByTemplateId(Long templateId) {
        checkTemplateExists(templateId);
        List<RptValidator> list = baseMapper.selectByTemplateId(templateId);
        return list.stream().map(this::toVO).toList();
    }

    @Override
    public ValidatorVO getValidatorDetail(Long id) {
        RptValidator v = baseMapper.selectById(id);
        if (v == null) throw new BusinessException("校验规则不存在");
        return toVO(v);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createValidator(ValidatorDTO dto) {
        checkTemplateExists(dto.getTemplateId());
        RptValidator validator = convertToEntity(dto);
        baseMapper.insert(validator);
        log.info("创建校验规则: id={}, name={}, type={}", validator.getId(), dto.getName(), dto.getType());
        return validator.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateValidator(ValidatorDTO dto) {
        if (dto.getId() == null) throw new BusinessException("规则ID不能为空");
        RptValidator existing = baseMapper.selectById(dto.getId());
        if (existing == null) throw new BusinessException("校验规则不存在");
        RptValidator updated = convertToEntity(dto);
        updated.setId(existing.getId());
        updated.setTemplateId(existing.getTemplateId());
        baseMapper.updateById(updated);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteValidator(Long id) {
        baseMapper.deleteById(id);
    }

    /**
     * 核心校验方法：根据模板配置的校验规则，对传入的数据进行校验
     */
    @Override
    public Map<String, String> validateData(Long templateId, Map<String, String> cellData, Integer trigger) {
        List<RptValidator> validators = baseMapper.selectByTemplateId(templateId);
        Map<String, String> errors = new LinkedHashMap<>();

        for (RptValidator v : validators) {
            // 过滤校验时机不匹配的规则
            if (v.getValidateTrigger() != null && !Objects.equals(v.getValidateTrigger(), trigger)) {
                continue;
            }

            // 解析目标范围
            Set<String> targetCells = resolveTargetCells(v);

            for (String cellKey : targetCells) {
                String value = cellData.get(cellKey);
                String error = doValidate(v, cellKey, value);
                if (error != null) {
                    errors.put(cellKey, error);
                }
            }
        }

        return errors;
    }

    /**
     * 从设计器JSON批量导入校验规则
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importFromDesignerJson(Long templateId, List<ValidatorDef> validators) {
        // 先删除该模板下所有旧规则
        LambdaQueryWrapper<RptValidator> delWrapper = new LambdaQueryWrapper<RptValidator>()
                .eq(RptValidator::getTemplateId, templateId);
        baseMapper.delete(delWrapper);

        if (validators == null || validators.isEmpty()) return;

        int priority = 0;
        for (ValidatorDef def : validators) {
            RptValidator entity = new RptValidator();
            entity.setTemplateId(templateId);
            entity.setValidatorName(def.getName());
            entity.setValidatorType(mapTypeToInt(def.getType()));
            entity.setTargetRows(listToStr(def.getTargetRows()));
            entity.setTargetColumns(listToStr(def.getTargetColumns()));
            try {
                entity.setRuleConfig(objectMapper.writeValueAsString(def.getRuleConfig()));
            } catch (JsonProcessingException e) {
                entity.setRuleConfig("{}");
            }
            entity.setErrorMessage(def.getErrorMessage());
            entity.setValidateTrigger(mapTriggerToInt(def.getValidateTrigger()));
            entity.setPriority(priority++);
            entity.setStatus(1);
            baseMapper.insert(entity);
        }
        log.info("从设计器导入校验规则: templateId={}, count={}", templateId, validators.size());
    }

    @Override
    public List<ValidatorDef> exportToDesignerJson(Long templateId) {
        List<RptValidator> list = baseMapper.selectByTemplateId(templateId);
        List<ValidatorDef> result = new ArrayList<>();
        for (RptValidator v : list) {
            ValidatorDef def = new ValidatorDef();
            def.setName(v.getValidatorName());
            def.setType(mapIntToType(v.getValidatorType()));
            def.setTargetRows(strToList(v.getTargetRows()));
            def.setTargetColumns(strToList(v.getTargetColumns()));
            try {
                Map<String, Object> config = objectMapper.readValue(v.getRuleConfig(), Map.class);
                def.setRuleConfig(config != null ? config : Map.of());
            } catch (Exception e) {
                def.setRuleConfig(Map.of());
            }
            def.setErrorMessage(v.getErrorMessage());
            def.setValidateTrigger(mapIntToTrigger(v.getValidateTrigger()));
            def.setPriority(v.getPriority());
            result.add(def);
        }
        return result;
    }

    // ==================== 私有方法 ====================

    private String doValidate(RptValidator v, String cellKey, String value) {
        int type = v.getValidatorType();
        try {
            return switch (type) {
                case 1 -> validateNotNull(value, v.getErrorMessage());   // 非空
                case 2 -> validateRange(value, v);                       // 范围
                case 3 -> validateRegex(value, v);                       // 正则
                case 5 -> validateBusiness(value, v);                    // 业务规则（预留）
                default -> null;                                         // 自定义/其他跳过
            };
        } catch (Exception e) {
            log.warn("校验执行异常: cell={}, error={}", cellKey, e.getMessage());
            return v.getErrorMessage();
        }
    }

    private String validateNotNull(String value, String errorMsg) {
        if (StrUtil.isBlank(value)) return errorMsg;
        return null;
    }

    private String validateRange(String value, RptValidator v) throws JsonProcessingException {
        if (StrUtil.isBlank(value)) return null; // 非空由单独规则处理
        Map<String, Object> config = objectMapper.readValue(v.getRuleConfig(), Map.class);
        BigDecimal numValue = new BigDecimal(value);
        Object minObj = config.get("min");
        Object maxObj = config.get("max");
        if (minObj != null && numValue.compareTo(new BigDecimal(minObj.toString())) < 0) {
            return v.getErrorMessage();
        }
        if (maxObj != null && numValue.compareTo(new BigDecimal(maxObj.toString())) > 0) {
            return v.getErrorMessage();
        }
        return null;
    }

    private String validateRegex(String value, RptValidator v) throws JsonProcessingException {
        if (StrUtil.isBlank(value)) return null;
        Map<String, Object> config = objectMapper.readValue(v.getRuleConfig(), Map.class);
        String patternStr = (String) config.get("pattern");
        if (StrUtil.isNotBlank(patternStr) && !Pattern.matches(patternStr, value)) {
            return v.getErrorMessage();
        }
        return null;
    }

    private String validateBusiness(String value, RptValidator v) {
        // 业务规则校验 — 预留扩展点
        // 可接入 Drools / Aviator / 自定义脚本引擎
        log.debug("业务规则校验(预留): ruleId={}", v.getId());
        return null;
    }

    private Set<String> resolveTargetCells(RptValidator v) {
        Set<String> cells = new HashSet<>();
        List<String> rows = strToList(v.getTargetRows());
        List<String> cols = strToList(v.getTargetColumns());

        if (rows.isEmpty()) rows = Collections.singletonList("*");
        if (cols.isEmpty()) cols = Collections.singletonList("*");

        boolean rowAll = rows.contains("*");
        boolean colAll = cols.contains("*");

        if (rowAll && colAll) {
            cells.add("*:*"); // 全部单元格
        } else if (rowAll) {
            for (String col : cols) cells.add("*:" + col);
        } else if (colAll) {
            for (String row : rows) cells.add(row + ":*");
        } else {
            for (String row : rows) {
                for (String col : cols) {
                    cells.add(row + ":" + col);
                }
            }
        }
        return cells;
    }

    private RptValidator convertToEntity(ValidatorDTO dto) {
        RptValidator v = new RptValidator();
        v.setTemplateId(dto.getTemplateId());
        v.setValidatorName(dto.getName());
        v.setValidatorType(mapTypeToInt(dto.getType()));
        v.setTargetRows(listToStr(dto.getTargetRows()));
        v.setTargetColumns(listToStr(dto.getTargetColumns()));
        try { v.setRuleConfig(objectMapper.writeValueAsString(dto.getRuleConfig())); }
        catch (JsonProcessingException e) { v.setRuleConfig("{}"); }
        v.setErrorMessage(dto.getErrorMessage());
        v.setValidateTrigger(mapTriggerToInt(dto.getValidateTrigger()));
        v.setPriority(dto.getPriority());
        v.setStatus(dto.getStatus());
        return v;
    }

    private ValidatorVO toVO(RptValidator v) {
        ValidatorVO vo = new ValidatorVO();
        vo.setId(v.getId());
        vo.setTemplateId(v.getTemplateId());
        vo.setName(v.getValidatorName());
        vo.setType(mapIntToType(v.getValidatorType()));
        vo.setTargetRows(strToList(v.getTargetRows()));
        vo.setTargetColumns(strToList(v.getTargetColumns()));
        try { vo.setRuleConfig(objectMapper.readValue(v.getRuleConfig(), Map.class)); }
        catch (Exception e) { vo.setRuleConfig(Map.of()); }
        vo.setErrorMessage(v.getErrorMessage());
        vo.setValidateTrigger(mapIntToTrigger(v.getValidateTrigger()));
        vo.setPriority(v.getPriority());
        vo.setStatus(v.getStatus());
        vo.setCreatedAt(v.getCreateTime());
        vo.setUpdatedAt(v.getUpdateTime());
        return vo;
    }

    private int mapTypeToInt(String type) {
        if (type == null) return 1;
        return switch (type.toLowerCase()) {
            case "not_null" -> 1;
            case "range" -> 2;
            case "regex" -> 3;
            case "custom" -> 4;
            case "business" -> 5;
            default -> 1;
        };
    }

    private String mapIntToType(Integer type) {
        if (type == null) return "not_null";
        return switch (type) {
            case 1 -> "not_null";
            case 2 -> "range";
            case 3 -> "regex";
            case 4 -> "custom";
            case 5 -> "business";
            default -> "not_null";
        };
    }

    private int mapTriggerToInt(String trigger) {
        if (trigger == null) return 2;
        return switch (trigger.toLowerCase()) {
            case "input" -> 1;
            case "save" -> 2;
            case "submit" -> 3;
            default -> 2;
        };
    }

    private String mapIntToTrigger(Integer trigger) {
        if (trigger == null) return "save";
        return switch (trigger) {
            case 1 -> "input";
            case 2 -> "save";
            case 3 -> "submit";
            default -> "save";
        };
    }

    private String listToStr(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return String.join(",", list);
    }

    private List<String> strToList(String str) {
        if (StrUtil.isBlank(str)) return List.of();
        return Arrays.asList(str.split(","));
    }

    private void checkTemplateExists(Long templateId) {
        RptTemplate t = templateMapper.selectById(templateId);
        if (t == null) throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
    }
}
