package com.groupreport.platform.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.groupreport.platform.common.Constants;
import com.groupreport.platform.common.ResultCode;
import com.groupreport.platform.dto.*;
import com.groupreport.platform.entity.RptData;
import com.groupreport.platform.entity.RptSubmit;
import com.groupreport.platform.exception.BusinessException;
import com.groupreport.platform.mapper.RptDataMapper;
import com.groupreport.platform.mapper.RptSubmitMapper;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportDataServiceImpl implements ReportDataService {

    private final RptDataMapper dataMapper;
    private final RptSubmitMapper submitMapper;
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

        Long userId = StpUtil.getLoginIdAsLong();
        int savedCount = 0;

        for (CellDataDTO cell : cells) {

            RptData existing = dataMapper.selectOne(
                    new LambdaQueryWrapper<RptData>()
                            .eq(RptData::getTemplateId, templateId)
                            .eq(RptData::getOrgId, orgId)
                            .eq(RptData::getPeriod, period)
                            .eq(RptData::getRowCode, cell.getRowCode())
                            .eq(RptData::getColumnCode, cell.getColumnCode())
            );

            if (existing != null) {
                updateCellData(existing, cell, userId);
                dataMapper.updateById(existing);
            } else {
                dataMapper.insert(createNewCellData(templateId, orgId, period, cell, userId));
            }

            savedCount++;
        }

        return savedCount;
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
        data.setValueText(cell.getValueText());
        data.setValueNumber(cell.getValueNumber());
        data.setDataType(cell.getDataType());
        data.setUpdateBy(userId);
        data.setUpdateTime(LocalDateTime.now());
    }

    private RptData createNewCellData(Long templateId, Long orgId, String period,
                                      CellDataDTO cell, Long userId) {

        RptData data = new RptData();
        data.setTemplateId(templateId);
        data.setOrgId(orgId);
        data.setPeriod(period);
        data.setRowCode(cell.getRowCode());
        data.setColumnCode(cell.getColumnCode());
        data.setValueText(cell.getValueText());
        data.setValueNumber(cell.getValueNumber());
        data.setDataType(cell.getDataType());
        data.setCreateBy(userId);

        return data;
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