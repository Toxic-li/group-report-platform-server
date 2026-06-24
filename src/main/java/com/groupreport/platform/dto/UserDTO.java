package com.groupreport.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 用户信息DTO
 */
@Data
@Schema(description = "用户信息")
public class UserDTO {

    @Schema(description = "用户ID（新增时为空）")
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度在3-32个字符之间")
    @Schema(description = "用户名", required = true)
    private String username;

    @Size(min = 6, max = 32, message = "密码长度在6-32个字符之间")
    @Schema(description = "密码（新增时必填，修改时可为空）")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "真实姓名", required = true)
    private String realName;

    @Schema(description = "昵称")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "性别：0-未知 1-男 2-女")
    private Integer gender;

    @Schema(description = "所属组织ID")
    private Long orgId;

    @Schema(description = "职位")
    private String position;

    @Schema(description = "角色ID列表")
    private List<Long> roleIds;
}
