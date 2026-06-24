package com.groupreport.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色VO
 */
@Data
@Schema(description = "角色信息")
public class RoleVO {

    @Schema(description = "角色ID")
    private Long id;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "数据范围")
    private Integer dataScope;

    @Schema(description = "数据范围名称")
    private String dataScopeName;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "用户数量")
    private Long userCount;
}
