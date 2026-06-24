package com.groupreport.platform.controller;

import com.groupreport.platform.common.PageResult;
import com.groupreport.platform.common.Result;
import com.groupreport.platform.dto.UserDTO;
import com.groupreport.platform.service.UserService;
import com.groupreport.platform.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理", description = "用户的增删改查接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户列表")
    @GetMapping("/page")
    public Result<PageResult<UserVO>> pageUsers(
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "真实姓名") @RequestParam(required = false) String realName,
            @Parameter(description = "组织ID") @RequestParam(required = false) Long orgId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") long size) {
        return Result.success(userService.pageUsers(username, realName, orgId, status, current, size));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public Result<UserVO> getUserDetail(@PathVariable Long id) {
        return Result.success(userService.getUserDetail(id));
    }

    @Operation(summary = "创建用户")
    @PostMapping
    public Result<Long> createUser(@Valid @RequestBody UserDTO userDTO) {
        return Result.success(userService.createUser(userDTO));
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        userDTO.setId(id);
        userService.updateUser(userDTO);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @Operation(summary = "重置密码")
    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success();
    }

    @Operation(summary = "更新用户状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }
}
