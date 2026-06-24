package com.groupreport.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.groupreport.platform.dto.RoleDTO;
import com.groupreport.platform.entity.SysRole;
import com.groupreport.platform.vo.RoleVO;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService extends IService<SysRole> {

    /**
     * 获取所有启用的角色列表
     * @return 角色列表
     */
    List<RoleVO> getAllRoles();

    /**
     * 获取角色详情
     * @param roleId 角色ID
     * @return 角色详情
     */
    RoleVO getRoleDetail(Long roleId);

    /**
     * 创建角色
     * @param roleDTO 角色信息
     * @return 角色ID
     */
    Long createRole(RoleDTO roleDTO);

    /**
     * 更新角色
     * @param roleDTO 角色信息
     */
    void updateRole(RoleDTO roleDTO);

    /**
     * 删除角色
     * @param roleId 角色ID
     */
    void deleteRole(Long roleId);
}
