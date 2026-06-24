package com.groupreport.platform.dto;

import lombok.Data;

import java.util.List;

/**
 * 前端设计器专用DTO（Univer / 类Excel结构）
 */
@Data
public class DesignerTemplateDTO {

    private Long id;

    /** 对应前端 code */
    private String code;

    /** 对应前端 name */
    private String name;

    private Integer templateType = 1;

    private String description;

    /** 行树 */
    private List<RowTreeDTO> rowTree;

    /** 列树 */
    private List<ColumnTreeDTO> columnTree;
}