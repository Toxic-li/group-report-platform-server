package com.groupreport.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色DTO
 */
@Data
@Schema(description = "角色信息")
public class RoleDTO {

    @Schema(description = "角色ID（新增时为空）")
    private Long id;

    @NotBlank(message = "角色编码不能为空")
    @Size(max = 64, message = "角色编码长度不能超过64个字符")
    @Schema(description = "角色编码", required = true)
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 128, message = "角色名称长度不能超过128个字符")
    @Schema(description = "角色名称", required = true)
    private String roleName;

    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "数据范围：1-全部 2-本部门及以下 3-本部门 4-本人")
    private Integer dataScope = 1;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status = 1;
}
