package com.groupreport.platform.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.groupreport.platform.common.Constants;
import com.groupreport.platform.common.ResultCode;
import com.groupreport.platform.dto.*;
import com.groupreport.platform.entity.RptData;
import com.groupreport.platform.entity.RptSubmit;
import com.groupreport.platform.exception.BusinessException;
import com.groupreport.platform.mapper.RptDataMapper;
import com.groupreport.platform.mapper.RptSubmitMapper;
import com.groupreport.platform.mapper.RptTemplateRowMapper;
import com.groupreport.platform.mapper.RptTemplateColumnMapper;
import com.groupreport.platform.entity.RptTemplateRow;
import com.groupreport.platform.entity.RptTemplateColumn;
import com.groupreport.platform.service.ReportDataService;
import com.groupreport.platform.service.ReportTemplateService;
import com.groupreport.platform.vo.CellValueVO;
import com.groupreport.platform.vo.ColumnVO;
import com.groupreport.platform.vo.ReportDataVO;
import com.groupreport.platform.vo.RowVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportDataServiceImpl extends ServiceImpl<RptDataMapper, RptData> implements ReportDataService {

    private final RptDataMapper dataMapper;
    private final RptSubmitMapper submitMapper;
    private final RptTemplateRowMapper rowMapper;
    private final RptTemplateColumnMapper columnMapper;
    private final ReportTemplateService templateService;

    @Override
    public ReportDataVO getReportData(Long templateId, Long orgId, String period) {

        DesignerTemplateDTO template = templateService.getTemplateDetail(templateId);

        RptSubmit submit = getOrCreateSubmit(templateId, orgId, period);

        List<RptData> dataList = dataMapper.selectList(
                new LambdaQueryWrapper<RptData>()
                        .eq(RptData::getTemplateId, templateId)
                        .eq(RptData::getOrgId, orgId)
                        .eq(RptData::getPeriod, period)
        );

        Map<String, CellValueVO> dataMap = dataList.stream()
                .collect(Collectors.toMap(
                        d -> d.getRowCode() + ":" + d.getColumnCode(),
                        this::convertToCellValueVO,
                        (v1, v2) -> v1
                ));

        int totalCells = calculateTotalCells(template);

        int filledCells = (int) dataList.stream()
                .filter(d ->
                        StringUtils.hasText(d.getValueText())
                                || d.getValueNumber() != null
                )
                .count();

        double completeRate = totalCells == 0 ? 0 :
                (double) filledCells / totalCells * 100;

        return ReportDataVO.builder()
                .submitId(submit.getId())
                .templateId(templateId)
                .orgId(orgId)
                .period(period)
                .submitStatus(submit.getSubmitStatus())
                .completeRate(Math.round(completeRate * 100.0) / 100.0)
                .totalCells(totalCells)
                .filledCells(filledCells)
                .rows(convertRows(template.getRowTree()))
                .columns(convertColumns(template.getColumnTree()))
                .data(dataMap)
                .remark(submit.getRemark())
                .createTime(submit.getCreateTime())
                .updateTime(submit.getUpdateTime())
                .build();
    }
    private List<RowVO> convertRows(List<RowTreeDTO> list) {
        if (list == null) return Collections.emptyList();

        return list.stream().map(r -> {
            RowVO vo = new RowVO();
            vo.setId(Long.valueOf(r.getId()));
            vo.setRowName(r.getName());
            vo.setLevel(r.getLevel());
            return vo;
        }).collect(Collectors.toList());
    }
    private List<ColumnVO> convertColumns(List<ColumnTreeDTO> list) {
        if (list == null) return Collections.emptyList();

        List<ColumnVO> result = new ArrayList<>();

        for (ColumnTreeDTO node : list) {
            ColumnVO vo = new ColumnVO();
            vo.setColumnCode(node.getId());
            vo.setColumnName(node.getTitle());
            result.add(vo);

            if (node.getChildren() != null) {
                result.addAll(convertColumns(node.getChildren()));
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> saveData(ReportDataSaveDTO saveDTO) {

        DesignerTemplateDTO template =
                templateService.getTemplateDetail(saveDTO.getTemplateId());

        RptSubmit submit = getExistingSubmit(
                saveDTO.getTemplateId(),
                saveDTO.getOrgId(),
                saveDTO.getPeriod()
        );

        if (submit != null &&
                submit.getSubmitStatus() == Constants.SubmitStatus.PENDING) {
            throw new BusinessException(ResultCode.PERIOD_ALREADY_SUBMITTED);
        }

        int savedCount = batchSaveCells(
                saveDTO.getTemplateId(),
                saveDTO.getOrgId(),
                saveDTO.getPeriod(),
                saveDTO.getCells()
        );

        Map<String, Object> result = new HashMap<>();
        result.put("savedCount", savedCount);
        result.put("message", "保存成功");
        result.put("timestamp", LocalDateTime.now());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSaveCells(Long templateId, Long orgId, String period, List<CellDataDTO> cells) {

        if (CollectionUtils.isEmpty(cells)) {
            return 0;
        }

        log.info("开始批量保存单元格数据: templateId={}, orgId={}, period={}, cellsCount={}",
                templateId, orgId, period, cells.size());

        Long userId = StpUtil.getLoginIdAsLong();
        List<RptData> insertList = new ArrayList<>();
        List<RptData> updateList = new ArrayList<>();

        for (CellDataDTO cell : cells) {
            log.debug("处理单元格: rowCode={}, columnCode={}, value={}, dataType={}",
                    cell.getRowCode(), cell.getColumnCode(), cell.getValue(), cell.getDataType());

            // 根据 rowCode 查询 rowId
            if (cell.getRowId() == null && StringUtils.hasText(cell.getRowCode())) {
                RptTemplateRow row = rowMapper.selectOne(
                        new LambdaQueryWrapper<RptTemplateRow>()
                                .eq(RptTemplateRow::getTemplateId, templateId)
                                .eq(RptTemplateRow::getRowCode, cell.getRowCode())
                );
                if (row != null) {
                    cell.setRowId(row.getId());
                    log.debug("查询到 rowId: {} for rowCode: {}", row.getId(), cell.getRowCode());
                } else {
                    log.warn("未找到 rowId for rowCode: {}", cell.getRowCode());
                }
            }

            // 根据 columnCode 查询 columnId
            if (cell.getColumnId() == null && StringUtils.hasText(cell.getColumnCode())) {
                RptTemplateColumn column = columnMapper.selectOne(
                        new LambdaQueryWrapper<RptTemplateColumn>()
                                .eq(RptTemplateColumn::getTemplateId, templateId)
                                .eq(RptTemplateColumn::getColumnCode, cell.getColumnCode())
                );
                if (column != null) {
                    cell.setColumnId(column.getId());
                    log.debug("查询到 columnId: {} for columnCode: {}", column.getId(), cell.getColumnCode());
                } else {
                    log.warn("未找到 columnId for columnCode: {}", cell.getColumnCode());
                }
            }

            // 查询现有数据
            RptData existing = dataMapper.selectOne(
                    new LambdaQueryWrapper<RptData>()
                            .eq(RptData::getTemplateId, templateId)
                            .eq(RptData::getOrgId, orgId)
                            .eq(RptData::getPeriod, period)
                            .eq(RptData::getRowCode, cell.getRowCode())
                            .eq(RptData::getColumnCode, cell.getColumnCode())
            );

            if (existing != null) {
                // 更新现有数据
                updateCellData(existing, cell, userId);
                updateList.add(existing);
                log.debug("更新现有数据: rowCode={}, columnCode={}", cell.getRowCode(), cell.getColumnCode());
            } else {
                // 创建新数据
                insertList.add(createNewCellData(templateId, orgId, period, cell, userId));
                log.debug("创建新数据: rowCode={}, columnCode={}", cell.getRowCode(), cell.getColumnCode());
            }
        }

        // 批量插入新数据
        if (!insertList.isEmpty()) {
            log.info("批量插入 {} 条新数据", insertList.size());
            saveBatch(insertList);
        }

        // 批量更新现有数据
        if (!updateList.isEmpty()) {
            log.info("批量更新 {} 条数据", updateList.size());
            updateBatchById(updateList);
        }

        int totalCount = insertList.size() + updateList.size();
        log.info("批量保存完成: 插入 {} 条, 更新 {} 条, 总计 {} 条",
                insertList.size(), updateList.size(), totalCount);

        return totalCount;
    }

    @Override
    public Object getCellValue(Long templateId, Long orgId, String period,
                               String rowCode, String columnCode) {

        RptData data = dataMapper.selectOne(
                new LambdaQueryWrapper<RptData>()
                        .eq(RptData::getTemplateId, templateId)
                        .eq(RptData::getOrgId, orgId)
                        .eq(RptData::getPeriod, period)
                        .eq(RptData::getRowCode, rowCode)
                        .eq(RptData::getColumnCode, columnCode)
        );

        if (data == null) return null;

        return switch (data.getDataType()) {
            case 2 -> data.getValueNumber();
            case 3 -> data.getValueDate();
            default -> data.getValueText();
        };
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearData(Long templateId, Long orgId, String period) {
        dataMapper.delete(
                new LambdaQueryWrapper<RptData>()
                        .eq(RptData::getTemplateId, templateId)
                        .eq(RptData::getOrgId, orgId)
                        .eq(RptData::getPeriod, period)
        );
    }

    @Override
    public List<String> validateData(Long templateId, Long orgId, String period) {

        List<String> errors = new ArrayList<>();

        DesignerTemplateDTO template = templateService.getTemplateDetail(templateId);

        for (ColumnTreeDTO column : flattenColumns(template.getColumnTree())) {

            for (RowTreeDTO row : template.getRowTree()) {

                Object value = getCellValue(
                        templateId,
                        orgId,
                        period,
                        row.getId(),
                        column.getId()
                );

                if (value == null ||
                        (value instanceof String s && !StringUtils.hasText(s))) {

                    errors.add("[" + row.getName() + "-" + column.getTitle() + "] 必填");
                }
            }
        }

        return errors;
    }

    // ================= helpers =================

    private List<ColumnTreeDTO> flattenColumns(List<ColumnTreeDTO> tree) {
        List<ColumnTreeDTO> list = new ArrayList<>();
        if (tree == null) return list;

        for (ColumnTreeDTO node : tree) {
            list.add(node);
            if (node.getChildren() != null) {
                list.addAll(flattenColumns(node.getChildren()));
            }
        }
        return list;
    }

    private int calculateTotalCells(DesignerTemplateDTO template) {
        return template.getRowTree().size()
                * flattenColumns(template.getColumnTree()).size();
    }

    private RptSubmit getOrCreateSubmit(Long templateId, Long orgId, String period) {

        RptSubmit exist = getExistingSubmit(templateId, orgId, period);
        if (exist != null) return exist;

        RptSubmit submit = new RptSubmit();
        submit.setSubmitNo("SUB" + System.currentTimeMillis());
        submit.setTemplateId(templateId);
        submit.setOrgId(orgId);
        submit.setPeriod(period);
        submit.setSubmitStatus(Constants.SubmitStatus.DRAFT);
        submit.setDataCompleteRate(BigDecimal.ZERO);
        submit.setCreateBy(StpUtil.getLoginIdAsLong());

        submitMapper.insert(submit);
        return submit;
    }

    private RptSubmit getExistingSubmit(Long templateId, Long orgId, String period) {
        return submitMapper.selectOne(
                new LambdaQueryWrapper<RptSubmit>()
                        .eq(RptSubmit::getTemplateId, templateId)
                        .eq(RptSubmit::getOrgId, orgId)
                        .eq(RptSubmit::getPeriod, period)
                        .last("LIMIT 1")
        );
    }

    private void updateCellData(RptData data, CellDataDTO cell, Long userId) {
        // 设置行ID和列ID
        if (cell.getRowId() != null) {
            data.setRowId(cell.getRowId());
        }
        if (cell.getColumnId() != null) {
            data.setColumnId(cell.getColumnId());
        }

        // 优先使用前端传来的 dataType，否则自动识别
        if (cell.getDataType() != null) {
            setDataValueByType(data, cell);
        } else {
            identifyAndSetDataValue(data, cell);
        }

        // 设置公式标记
        if (StringUtils.hasText(cell.getFormula())) {
            data.setIsFormula(1);
        }

        // 设置数据来源（优先使用前端传来的值）
        if (cell.getSource() != null) {
            data.setSource(cell.getSource());
        }

        // 设置备注
        if (StringUtils.hasText(cell.getRemark())) {
            data.setRemark(cell.getRemark());
        }

        data.setUpdateBy(userId);
        data.setUpdateTime(LocalDateTime.now());
    }

    private RptData createNewCellData(Long templateId, Long orgId, String period,
                                      CellDataDTO cell, Long userId) {

        RptData data = new RptData();
        data.setTemplateId(templateId);
        data.setOrgId(orgId);
        data.setPeriod(period);

        // 设置行ID和列ID
        data.setRowId(cell.getRowId());
        data.setColumnId(cell.getColumnId());
        data.setRowCode(cell.getRowCode());
        data.setColumnCode(cell.getColumnCode());

        // 优先使用前端传来的 dataType，否则自动识别
        if (cell.getDataType() != null) {
            setDataValueByType(data, cell);
        } else {
            identifyAndSetDataValue(data, cell);
        }

        // 设置公式标记
        if (StringUtils.hasText(cell.getFormula())) {
            data.setIsFormula(1);
        }

        // 设置数据来源（优先使用前端传来的值，否则默认为手动录入）
        data.setSource(cell.getSource() != null ? cell.getSource() : 1);

        // 设置备注
        if (StringUtils.hasText(cell.getRemark())) {
            data.setRemark(cell.getRemark());
        }

        data.setDeleted(0);
        data.setCreateBy(userId);

        return data;
    }

    /**
     * 根据前端传来的数据类型设置值
     */
    private void setDataValueByType(RptData data, CellDataDTO cell) {
        data.setDataType(cell.getDataType());

        String value = cell.getValue();
        if (value == null) {
            value = cell.getRawValue();
        }

        switch (cell.getDataType()) {
            case 2: // 数字
                try {
                    if (StringUtils.hasText(value)) {
                        data.setValueNumber(new BigDecimal(value));
                        data.setValueText(value);
                    }
                } catch (NumberFormatException e) {
                    log.warn("数字格式错误: value={}, rowCode={}, columnCode={}",
                            value, cell.getRowCode(), cell.getColumnCode());
                }
                break;
            case 3: // 日期
                try {
                    if (StringUtils.hasText(value)) {
                        LocalDate dateValue = LocalDate.parse(value);
                        data.setValueDate(dateValue);
                        data.setValueText(value);
                    }
                } catch (Exception e) {
                    log.warn("日期格式错误: value={}, rowCode={}, columnCode={}",
                            value, cell.getRowCode(), cell.getColumnCode());
                }
                break;
            case 1: // 文本
            default:
                data.setValueText(value);
                break;
        }
    }

    /**
     * 自动识别数据类型并设置值
     */
    private void identifyAndSetDataValue(RptData data, CellDataDTO cell) {
        String value = cell.getValue();
        
        // 如果value为空，尝试使用valueText或valueNumber
        if (!StringUtils.hasText(value)) {
            if (cell.getValueNumber() != null) {
                // 如果提供了valueNumber，直接使用
                data.setValueNumber(cell.getValueNumber());
                data.setDataType(2); // 数字类型
                data.setValueText(cell.getValueNumber().toString());
            } else if (StringUtils.hasText(cell.getValueText())) {
                // 如果提供了valueText，直接使用
                data.setValueText(cell.getValueText());
                data.setDataType(1); // 文本类型
            } else {
                // 所有值都为空
                data.setDataType(1);
            }
            return;
        }
        
        // 尝试解析为数字
        try {
            BigDecimal numberValue = new BigDecimal(value);
            data.setValueNumber(numberValue);
            data.setValueText(value);
            data.setDataType(2); // 数字类型
            return;
        } catch (NumberFormatException e) {
            // 不是数字，继续尝试其他类型
        }
        
        // 尝试解析为日期
        try {
            LocalDate dateValue = LocalDate.parse(value);
            data.setValueDate(dateValue);
            data.setValueText(value);
            data.setDataType(3); // 日期类型
            return;
        } catch (Exception e) {
            // 不是日期，作为文本处理
        }
        
        // 默认作为文本处理
        data.setValueText(value);
        data.setDataType(1); // 文本类型
    }

    private CellValueVO convertToCellValueVO(RptData data) {
        return CellValueVO.builder()
                .text(data.getValueText())
                .number(data.getValueNumber())
                .dataType(data.getDataType())
                .isFormula(data.getIsFormula() == 1)
                .isModified(data.getIsModified() == 1)
                .source(data.getSource())
                .build();
    }
}