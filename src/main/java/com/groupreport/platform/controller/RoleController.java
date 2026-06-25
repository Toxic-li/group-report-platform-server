package com.groupreport.platform.controller;

import com.groupreport.platform.common.Result;
import com.groupreport.platform.dto.RoleDTO;
import com.groupreport.platform.service.RoleService;
import com.groupreport.platform.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@Tag(name = "角色管理", description = "角色的增删改查接口")
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "获取所有启用的角色列表")
    @GetMapping("/list")
    public Result<List<RoleVO>> getAllRoles() {
        return Result.success(roleService.getAllRoles());
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    public Result<RoleVO> getRoleDetail(@PathVariable Long id) {
        return Result.success(roleService.getRoleDetail(id));
    }

    @Operation(summary = "创建角色")
    @PostMapping
    public Result<Long> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        return Result.success(roleService.createRole(roleDTO));
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public Result<Void> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDTO roleDTO) {
        roleDTO.setId(id);
        roleService.updateRole(roleDTO);
        return Result.success();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }
}
