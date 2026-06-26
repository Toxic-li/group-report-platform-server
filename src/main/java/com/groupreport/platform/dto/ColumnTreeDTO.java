package com.groupreport.platform.dto;

import lombok.Data;

import java.util.List;

/**
 * 前端列树结构
 */
@Data
public class ColumnTreeDTO {

    private String id;

    private String name;

    private String code;

    private String title;

    private String type;

    private Integer width;

    private String align;

    private List<ColumnTreeDTO> children;
}