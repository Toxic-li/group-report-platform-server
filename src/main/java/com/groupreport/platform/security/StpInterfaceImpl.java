package com.groupreport.platform.security;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.groupreport.platform.entity.SysRole;
import com.groupreport.platform.mapper.SysRoleMapper;
import com.groupreport.platform.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限实现类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;

    /**
     * 获取用户权限码列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 当前版本使用RBAC，权限通过角色控制
        // 后续可扩展细粒度权限
        return new ArrayList<>();
    }

    /**
     * 获取用户角色列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.parseLong(loginId.toString());
        
        // 查询用户角色ID列表
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询角色编码列表
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysRole::getId, roleIds)
               .eq(SysRole::getStatus, 1);
        List<SysRole> roles = roleMapper.selectList(wrapper);

        return roles.stream()
                .map(SysRole::getRoleCode)
                .toList();
    }
}
