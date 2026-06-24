package com.groupreport.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.groupreport.platform.common.Constants;
import com.groupreport.platform.common.PageResult;
import com.groupreport.platform.common.ResultCode;
import com.groupreport.platform.dto.UserDTO;
import com.groupreport.platform.entity.SysOrg;
import com.groupreport.platform.entity.SysRole;
import com.groupreport.platform.entity.SysUser;
import com.groupreport.platform.entity.SysUserRole;
import com.groupreport.platform.exception.BusinessException;
import com.groupreport.platform.mapper.*;
import com.groupreport.platform.service.UserService;
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
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysOrgMapper orgMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public PageResult<UserVO> pageUsers(String username, String realName, Long orgId, Integer status,
                                          long current, long size) {
        Page<SysUser> page = new Page<>(current, size);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(username)) {
            wrapper.like(SysUser::getUsername, username);
        }
        if (StringUtils.hasText(realName)) {
            wrapper.like(SysUser::getRealName, realName);
        }
        if (orgId != null) {
            wrapper.eq(SysUser::getOrgId, orgId);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }

        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> userPage = baseMapper.selectPage(page, wrapper);

        // 转换为VO列表
        List<UserVO> voList = userPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, userPage.getTotal(), current, size);
    }

    @Override
    public UserVO getUserDetail(Long userId) {
        SysUser user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return convertToVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserDTO userDTO) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(SysUser::getUsername, userDTO.getUsername());
        if (baseMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        SysUser user = new SysUser();
        BeanUtils.copyProperties(userDTO, user);
        user.setPassword(passwordEncoder.encode(
                StringUtils.hasText(userDTO.getPassword()) ? userDTO.getPassword() : Constants.DEFAULT_PASSWORD));
        user.setStatus(Constants.Status.ENABLED);

        baseMapper.insert(user);

        // 分配角色
        assignRoles(user.getId(), userDTO.getRoleIds());

        log.info("创建用户成功: {}", user.getUsername());
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserDTO userDTO) {
        SysUser user = baseMapper.selectById(userDTO.getId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 检查用户名是否被其他用户使用
        if (!user.getUsername().equals(userDTO.getUsername())) {
            LambdaQueryWrapper<SysUser> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(SysUser::getUsername, userDTO.getUsername())
                       .ne(SysUser::getId, userDTO.getId());
            if (baseMapper.selectCount(checkWrapper) > 0) {
                throw new BusinessException("用户名已存在");
            }
        }

        // 更新基本信息（不更新密码）
        BeanUtils.copyProperties(userDTO, user, "password", "id");
        
        // 如果提供了新密码，则更新密码
        if (StringUtils.hasText(userDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        baseMapper.updateById(user);

        // 重新分配角色
        userRoleMapper.deleteByUserId(user.getId());
        assignRoles(user.getId(), userDTO.getRoleIds());

        log.info("更新用户成功: {}", user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        if (userId.equals(Constants.SUPER_ADMIN_ID)) {
            throw new BusinessException("不能删除超级管理员");
        }

        baseMapper.deleteById(userId);
        userRoleMapper.deleteByUserId(userId);
        log.info("删除用户成功: {}", userId);
    }

    @Override
    public void resetPassword(Long userId) {
        SysUser user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        user.setPassword(passwordEncoder.encode(Constants.DEFAULT_PASSWORD));
        baseMapper.updateById(user);
        log.info("重置密码成功: {}", userId);
    }

    @Override
    public void updateStatus(Long userId, Integer status) {
        SysUser user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        user.setStatus(status);
        baseMapper.updateById(user);
        log.info("更新用户状态成功: {} -> {}", userId, status);
    }

    /**
     * 分配角色
     */
    private void assignRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }

        for (Long roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setCreateTime(LocalDateTime.now());
            userRoleMapper.insert(userRole);
        }
    }

    /**
     * 转换为VO
     */
    private UserVO convertToVO(SysUser user) {
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
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(user.getId());
        if (!roleIds.isEmpty()) {
            LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(SysRole::getId, roleIds);
            List<SysRole> roles = roleMapper.selectList(wrapper);
            vo.setRoles(roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList()));
        }

        return vo;
    }
}
