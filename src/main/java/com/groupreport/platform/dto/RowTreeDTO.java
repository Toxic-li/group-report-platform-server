package com.groupreport.platform.dto;

import lombok.Data;

import java.util.List;

/**
 * 前端行树结构
 */
@Data
public class RowTreeDTO {

    private String id;

    private String name;

    private String code;

    private Integer level;

    private Boolean isSummary;

    private String summaryType;

    private List<RowTreeDTO> children;
}