package com.groupreport.platform.controller;

import com.groupreport.platform.common.Result;
import com.groupreport.platform.dto.LoginDTO;
import com.groupreport.platform.service.AuthService;
import com.groupreport.platform.vo.LoginVO;
import com.groupreport.platform.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "登录、登出、获取当前用户等接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/current")
    public Result<UserVO> getCurrentUser() {
        return Result.success(authService.getCurrentUser());
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestParam String oldPassword,
                                        @RequestParam String newPassword) {
        authService.changePassword(oldPassword, newPassword);
        return Result.success();
    }
}
