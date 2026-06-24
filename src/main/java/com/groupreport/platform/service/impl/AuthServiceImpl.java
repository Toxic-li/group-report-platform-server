package com.groupreport.platform.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.groupreport.platform.common.Constants;
import com.groupreport.platform.common.PageResult;
import com.groupreport.platform.common.ResultCode;
import com.groupreport.platform.dto.LoginDTO;
import com.groupreport.platform.dto.UserDTO;
import com.groupreport.platform.entity.SysOrg;
import com.groupreport.platform.entity.SysRole;
import com.groupreport.platform.entity.SysUser;
import com.groupreport.platform.entity.SysUserRole;
import com.groupreport.platform.exception.BusinessException;
import com.groupreport.platform.mapper.*;
import com.groupreport.platform.security.StpInterfaceImpl;
import com.groupreport.platform.service.AuthService;
import com.groupreport.platform.service.UserService;
import com.groupreport.platform.vo.LoginVO;
import com.groupreport.platform.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysOrgMapper orgMapper;
    private final StpInterfaceImpl stpInterface;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 1. 查询用户
        SysUser user = userMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.LOGIN_ERROR);
        }

        // 2. 校验密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_ERROR);
        }

        // 3. 检查账号状态
        if (user.getStatus() == Constants.Status.DISABLED) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 4. Sa-Token 登录
        StpUtil.login(user.getId());

        // 5. 获取Token
        String token = StpUtil.getTokenValue();

        // 6. 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        // TODO: 获取真实IP
        user.setLastLoginIp("127.0.0.1");
        userMapper.updateById(user);

        // 7. 查询角色
        List<String> roles = stpInterface.getRoleList(user.getId(), "login");

        // 8. 查询组织名称
        String orgName = null;
        if (user.getOrgId() != null) {
            SysOrg org = orgMapper.selectById(user.getOrgId());
            if (org != null) {
                orgName = org.getOrgName();
            }
        }

        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .orgId(user.getOrgId())
                .orgName(orgName)
                .roles(roles)
                .loginTime(LocalDateTime.now())
                .build();
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public UserVO getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        return getUserDetail(userId);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        SysUser user = userMapper.selectById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        log.info("用户 {} 修改密码成功", userId);
    }

    private UserVO getUserDetail(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);

        // 查询组织名称
        if (user.getOrgId() != null) {
            SysOrg org = orgMapper.selectById(user.getOrgId());
            if (org != null) {
                vo.setOrgName(org.getOrgName());
            }
        }

        // 查询角色
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        if (!roleIds.isEmpty()) {
            LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(SysRole::getId, roleIds);
            List<SysRole> roles = roleMapper.selectList(wrapper);
            vo.setRoles(roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList()));
        }

        return vo;
    }
}
