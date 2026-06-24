package com.groupreport.platform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.groupreport.platform.common.Constants;
import com.groupreport.platform.common.ResultCode;
import com.groupreport.platform.dto.*;
import com.groupreport.platform.entity.RptTemplate;
import com.groupreport.platform.entity.RptTemplateColumn;
import com.groupreport.platform.entity.RptTemplateRow;
import com.groupreport.platform.exception.BusinessException;
import com.groupreport.platform.mapper.RptTemplateColumnMapper;
import com.groupreport.platform.mapper.RptTemplateMapper;
import com.groupreport.platform.mapper.RptTemplateRowMapper;
import com.groupreport.platform.service.ReportDesignerService;
import com.groupreport.platform.service.ReportTemplateService;
import com.groupreport.platform.vo.ReportDesignerTemplateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportTemplateServiceImpl
        extends ServiceImpl<RptTemplateMapper, RptTemplate>
        implements ReportTemplateService {

    private final RptTemplateRowMapper rowMapper;
    private final RptTemplateColumnMapper columnMapper;
    private final ReportDesignerService designerService;

    // =========================
    // 查询模板详情
    // =========================
    @Override
    @Cacheable(value = "template:detail", key = "#templateId")
    public DesignerTemplateDTO getTemplateDetail(Long templateId) {

        RptTemplate template = baseMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }

        List<RptTemplateRow> rows = rowMapper.selectList(
                new LambdaQueryWrapper<RptTemplateRow>()
                        .eq(RptTemplateRow::getTemplateId, templateId)
                        .orderByAsc(RptTemplateRow::getSortOrder)
        );

        List<RptTemplateColumn> columns = columnMapper.selectList(
                new LambdaQueryWrapper<RptTemplateColumn>()
                        .eq(RptTemplateColumn::getTemplateId, templateId)
                        .orderByAsc(RptTemplateColumn::getSortOrder)
        );

        DesignerTemplateDTO dto = new DesignerTemplateDTO();
        dto.setId(template.getId());
        dto.setCode(template.getTemplateCode());
        dto.setName(template.getTemplateName());
        dto.setTemplateType(template.getTemplateType());
        dto.setDescription(template.getDescription());

        dto.setRowTree(buildRowTree(rows));
        dto.setColumnTree(buildColumnTree(columns));

        return dto;
    }

    // =========================
    // 已发布模板列表
    // =========================
    @Override
    public List<DesignerTemplateDTO> getPublishedTemplates() {

        List<RptTemplate> templates = baseMapper.selectList(
                new LambdaQueryWrapper<RptTemplate>()
                        .eq(RptTemplate::getStatus, Constants.TemplateStatus.PUBLISHED)
                        .eq(RptTemplate::getDeleted, 0)
                        .orderByDesc(RptTemplate::getUpdateTime)
        );

        if (templates.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> ids = templates.stream()
                .map(RptTemplate::getId)
                .collect(Collectors.toList());

        List<RptTemplateRow> rows = rowMapper.selectList(
                new LambdaQueryWrapper<RptTemplateRow>()
                        .in(RptTemplateRow::getTemplateId, ids)
        );

        List<RptTemplateColumn> columns = columnMapper.selectList(
                new LambdaQueryWrapper<RptTemplateColumn>()
                        .in(RptTemplateColumn::getTemplateId, ids)
        );

        Map<Long, List<RptTemplateRow>> rowMap =
                rows.stream().collect(Collectors.groupingBy(RptTemplateRow::getTemplateId));

        Map<Long, List<RptTemplateColumn>> colMap =
                columns.stream().collect(Collectors.groupingBy(RptTemplateColumn::getTemplateId));

        return templates.stream().map(t -> {
            DesignerTemplateDTO dto = new DesignerTemplateDTO();

            dto.setId(t.getId());
            dto.setCode(t.getTemplateCode());
            dto.setName(t.getTemplateName());
            dto.setTemplateType(t.getTemplateType());
            dto.setDescription(t.getDescription());

            dto.setRowTree(buildRowTree(
                    rowMap.getOrDefault(t.getId(), Collections.emptyList())
            ));

            dto.setColumnTree(buildColumnTree(
                    colMap.getOrDefault(t.getId(), Collections.emptyList())
            ));

            return dto;
        }).collect(Collectors.toList());
    }

    // =========================
    // 创建模板
    // =========================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTemplate(TemplateDTO templateDTO) {

        checkTemplateCodeExists(null, templateDTO.getTemplateCode());

        RptTemplate template = new RptTemplate();
        BeanUtils.copyProperties(templateDTO, template);
        template.setStatus(Constants.TemplateStatus.DRAFT);
        template.setVersion(1);

        if (templateDTO.getRows() != null) {
            template.setRowCount(templateDTO.getRows().size());
        }
        if (templateDTO.getColumns() != null) {
            template.setColumnCount(templateDTO.getColumns().size());
        }

        baseMapper.insert(template);

        saveRows(template.getId(), templateDTO.getRows(), null);
        saveColumns(template.getId(), templateDTO.getColumns(), null);

        return template.getId();
    }

    // =========================
    // 更新模板
    // =========================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTemplate(TemplateDTO templateDTO) {

        RptTemplate template = baseMapper.selectById(templateDTO.getId());
        if (template == null) {
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }

        checkTemplateCodeExists(templateDTO.getId(), templateDTO.getTemplateCode());

        BeanUtils.copyProperties(templateDTO, template, "id", "status", "version");

        if (templateDTO.getRows() != null) {
            template.setRowCount(templateDTO.getRows().size());
        }
        if (templateDTO.getColumns() != null) {
            template.setColumnCount(templateDTO.getColumns().size());
        }

        baseMapper.updateById(template);

        deleteRowsAndColumns(templateDTO.getId());

        saveRows(templateDTO.getId(), templateDTO.getRows(), null);
        saveColumns(templateDTO.getId(), templateDTO.getColumns(), null);
    }

    // =========================
    // 发布模板
    // =========================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportDesignerTemplateVO publishTemplate(Long templateId) {

        RptTemplate template = baseMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }

        if (Objects.equals(template.getStatus(), Constants.TemplateStatus.PUBLISHED)) {
            throw new BusinessException("模板已发布");
        }

        template.setStatus(Constants.TemplateStatus.PUBLISHED);
        template.setVersion(template.getVersion() + 1);

        baseMapper.updateById(template);

        return designerService.loadFullTemplate(templateId);
    }

    // =========================
    // 禁用模板
    // =========================
    @Override
    public void disableTemplate(Long templateId) {

        RptTemplate template = baseMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }

        template.setStatus(Constants.TemplateStatus.DISABLED);
        baseMapper.updateById(template);
    }

    // =========================
    // 删除模板
    // =========================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplate(Long templateId) {

        baseMapper.deleteById(templateId);
        deleteRowsAndColumns(templateId);
    }

    // =========================
    // 校验模板编码
    // =========================
    private void checkTemplateCodeExists(Long excludeId, String code) {

        LambdaQueryWrapper<RptTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RptTemplate::getTemplateCode, code);

        if (excludeId != null) {
            wrapper.ne(RptTemplate::getId, excludeId);
        }

        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.TEMPLATE_CODE_EXISTS);
        }
    }

    // =========================
    // 删除行列
    // =========================
    private void deleteRowsAndColumns(Long templateId) {

        rowMapper.delete(new LambdaQueryWrapper<RptTemplateRow>()
                .eq(RptTemplateRow::getTemplateId, templateId));

        columnMapper.delete(new LambdaQueryWrapper<RptTemplateColumn>()
                .eq(RptTemplateColumn::getTemplateId, templateId));
    }

    // =========================
    // 保存行
    // =========================
    private void saveRows(Long templateId, List<RowConfigDTO> rows, Long parentId) {

        if (rows == null || rows.isEmpty()) return;

        int i = 1;

        for (RowConfigDTO dto : rows) {

            RptTemplateRow row = new RptTemplateRow();
            row.setTemplateId(templateId);
            row.setRowName(dto.getRowName());
            row.setRowCode(StrUtil.isNotBlank(dto.getRowCode())
                    ? dto.getRowCode()
                    : "ROW_" + System.nanoTime());

            row.setParentId(parentId == null ? 0L : parentId);
            row.setSortOrder(dto.getSortOrder() == null ? i++ : dto.getSortOrder());
            row.setLevel(dto.getLevel() == null ? 1 : dto.getLevel());

            rowMapper.insert(row);
        }
    }

    // =========================
    // 保存列
    // =========================
    private void saveColumns(Long templateId, List<ColumnConfigDTO> columns, Long parentId) {

        if (columns == null || columns.isEmpty()) return;

        int i = 1;

        for (ColumnConfigDTO dto : columns) {

            RptTemplateColumn col = new RptTemplateColumn();
            col.setTemplateId(templateId);
            col.setColumnName(dto.getColumnName());
            col.setColumnCode(StrUtil.isNotBlank(dto.getColumnCode())
                    ? dto.getColumnCode()
                    : "COL_" + System.nanoTime());

            col.setParentId(parentId == null ? 0L : parentId);
            col.setSortOrder(dto.getSortOrder() == null ? i++ : dto.getSortOrder());
            col.setWidth(dto.getWidth() == null ? 100 : dto.getWidth());

            columnMapper.insert(col);
        }
    }

    // =========================
    // Row Tree
    // =========================
    private List<RowTreeDTO> buildRowTree(List<RptTemplateRow> list) {

        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, RowTreeDTO> map = new HashMap<>();

        for (RptTemplateRow row : list) {
            RowTreeDTO dto = new RowTreeDTO();
            dto.setId(String.valueOf(row.getId()));
            dto.setName(row.getRowName());
            dto.setLevel(row.getLevel());

            map.put(row.getId(), dto); // ✅ Long key
        }

        List<RowTreeDTO> root = new ArrayList<>();

        for (RptTemplateRow row : list) {

            RowTreeDTO dto = map.get(row.getId());

            Long parentId = row.getParentId();

            if (parentId == null || parentId == 0) {
                root.add(dto);
            } else {
                RowTreeDTO parent = map.get(parentId); // ✅

                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(dto);
                } else {
                    root.add(dto);
                }
            }
        }

        return root;
    }

    // =========================
    // Column Tree
    // =========================
    private List<ColumnTreeDTO> buildColumnTree(List<RptTemplateColumn> list) {

        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, ColumnTreeDTO> map = new HashMap<>();

        // 1. build nodes
        for (RptTemplateColumn col : list) {
            ColumnTreeDTO dto = new ColumnTreeDTO();
            dto.setId(String.valueOf(col.getId()));
            dto.setTitle(col.getColumnName());
            dto.setType(String.valueOf(col.getColumnType()));
            dto.setWidth(col.getWidth());
            dto.setAlign(String.valueOf(col.getAlign()));

            map.put(col.getId(), dto); // ✅ Long key
        }

        List<ColumnTreeDTO> root = new ArrayList<>();

        // 2. build tree
        for (RptTemplateColumn col : list) {

            ColumnTreeDTO dto = map.get(col.getId());

            Long parentId = col.getParentId();

            if (parentId == null || parentId == 0) {
                root.add(dto);
            } else {
                ColumnTreeDTO parent = map.get(parentId); // ✅ 不再 String.valueOf

                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(dto);
                } else {
                    root.add(dto);
                }
            }
        }

        return root;
    }
}