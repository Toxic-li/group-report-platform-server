package com.groupreport.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 组织机构树形VO
 */
@Data
@Schema(description = "组织机构信息")
public class OrgVO {

    @Schema(description = "组织ID")
    private Long id;

    @Schema(description = "父级ID")
    private Long parentId;

    @Schema(description = "组织编码")
    private String orgCode;

    @Schema(description = "组织名称")
    private String orgName;

    @Schema(description = "组织类型")
    private Integer orgType;

    @Schema(description = "组织类型名称")
    private String orgTypeName;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "负责人")
    private String leader;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "层级")
    private Integer level;

    @Schema(description = "子节点列表")
    private List<OrgVO> children;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
