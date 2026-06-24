package com.groupreport.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.groupreport.platform.common.Constants;
import com.groupreport.platform.common.ResultCode;
import com.groupreport.platform.dto.RoleDTO;
import com.groupreport.platform.entity.SysRole;
import com.groupreport.platform.entity.SysUserRole;
import com.groupreport.platform.exception.BusinessException;
import com.groupreport.platform.mapper.SysRoleMapper;
import com.groupreport.platform.mapper.SysUserRoleMapper;
import com.groupreport.platform.service.RoleService;
import com.groupreport.platform.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements RoleService {

    private final SysUserRoleMapper userRoleMapper;

    @Override
    public List<RoleVO> getAllRoles() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getStatus, Constants.Status.ENABLED)
               .orderByAsc(SysRole::getSortOrder);
        List<SysRole> roles = baseMapper.selectList(wrapper);

        return roles.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public RoleVO getRoleDetail(Long roleId) {
        SysRole role = baseMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCode.ROLE_NOT_FOUND);
        }
        return convertToVO(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRole(RoleDTO roleDTO) {
        // 检查编码是否已存在
        LambdaQueryWrapper<SysRole> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(SysRole::getRoleCode, roleDTO.getRoleCode());
        if (baseMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException(ResultCode.ROLE_CODE_EXISTS);
        }

        SysRole role = new SysRole();
        BeanUtils.copyProperties(roleDTO, role);
        baseMapper.insert(role);

        log.info("创建角色成功: {}", role.getRoleName());
        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleDTO roleDTO) {
        SysRole role = baseMapper.selectById(roleDTO.getId());
        if (role == null) {
            throw new BusinessException(ResultCode.ROLE_NOT_FOUND);
        }

        // 检查编码是否被其他角色使用
        if (!role.getRoleCode().equals(roleDTO.getRoleCode())) {
            LambdaQueryWrapper<SysRole> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(SysRole::getRoleCode, roleDTO.getRoleCode())
                       .ne(SysRole::getId, roleDTO.getId());
            if (baseMapper.selectCount(checkWrapper) > 0) {
                throw new BusinessException(ResultCode.ROLE_CODE_EXISTS);
            }
        }

        BeanUtils.copyProperties(roleDTO, role, "id");
        baseMapper.updateById(role);
        log.info("更新角色成功: {}", role.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        // 检查是否有用户使用该角色
        LambdaQueryWrapper<SysUserRole> userCheck = new LambdaQueryWrapper<>();
        userCheck.eq(SysUserRole::getRoleId, roleId);
        Long userCount = userRoleMapper.selectCount(userCheck);
        if (userCount > 0) {
            throw new BusinessException("该角色下还有用户，无法删除");
        }

        baseMapper.deleteById(roleId);
        log.info("删除角色成功: {}", roleId);
    }

    /**
     * 转换为VO
     */
    private RoleVO convertToVO(SysRole role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);

        // 设置数据范围名称
        switch (role.getDataScope()) {
            case Constants.DataScope.ALL:
                vo.setDataScopeName("全部数据");
                break;
            case Constants.DataScope.DEPT_AND_BELOW:
                vo.setDataScopeName("本部门及以下");
                break;
            case Constants.DataScope.ONLY_DEPT:
                vo.setDataScopeName("仅本部门");
                break;
            case Constants.DataScope.ONLY_SELF:
                vo.setDataScopeName("仅本人");
                break;
            default:
                vo.setDataScopeName("未知");
        }

        // 查询使用该角色的用户数
        LambdaQueryWrapper<SysUserRole> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(SysUserRole::getRoleId, role.getId());
        vo.setUserCount(userRoleMapper.selectCount(countWrapper));

        return vo;
    }
}
