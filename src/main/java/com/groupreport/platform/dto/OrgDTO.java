package com.groupreport.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 组织机构DTO
 */
@Data
@Schema(description = "组织机构信息")
public class OrgDTO {

    @Schema(description = "组织ID（新增时为空）")
    private Long id;

    @Schema(description = "父级ID，0为顶级")
    private Long parentId = 0L;

    @NotBlank(message = "组织编码不能为空")
    @Size(max = 64, message = "组织编码长度不能超过64个字符")
    @Schema(description = "组织编码", required = true)
    private String orgCode;

    @NotBlank(message = "组织名称不能为空")
    @Size(max = 128, message = "组织名称长度不能超过128个字符")
    @Schema(description = "组织名称", required = true)
    private String orgName;

    @Schema(description = "组织类型：1-集团 2-子公司 3-部门 4-小组")
    private Integer orgType;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "负责人")
    private String leader;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status = 1;

    @Schema(description = "备注")
    private String remark;
}
