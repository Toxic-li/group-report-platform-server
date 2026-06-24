package com.groupreport.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.groupreport.platform.dto.UserDTO;
import com.groupreport.platform.entity.SysUser;
import com.groupreport.platform.vo.UserVO;
import com.groupreport.platform.common.PageResult;

/**
 * 用户服务接口
 */
public interface UserService extends IService<SysUser> {

    /**
     * 分页查询用户列表
     * @param username 用户名（模糊查询）
     * @param realName 真实姓名（模糊查询）
     * @param orgId 组织ID
     * @param status 状态
     * @param current 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<UserVO> pageUsers(String username, String realName, Long orgId, Integer status,
                                  long current, long size);

    /**
     * 获取用户详情（含角色信息）
     * @param userId 用户ID
     * @return 用户详情
     */
    UserVO getUserDetail(Long userId);

    /**
     * 创建用户
     * @param userDTO 用户信息
     * @return 用户ID
     */
    Long createUser(UserDTO userDTO);

    /**
     * 更新用户
     * @param userDTO 用户信息
     */
    void updateUser(UserDTO userDTO);

    /**
     * 删除用户
     * @param userId 用户ID
     */
    void deleteUser(Long userId);

    /**
     * 重置密码
     * @param userId 用户ID
     */
    void resetPassword(Long userId);

    /**
     * 更新用户状态
     * @param userId 用户ID
     * @param status 状态
     */
    void updateStatus(Long userId, Integer status);
}
