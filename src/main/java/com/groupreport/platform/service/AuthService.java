package com.groupreport.platform.service;

import com.groupreport.platform.dto.LoginDTO;
import com.groupreport.platform.dto.UserDTO;
import com.groupreport.platform.vo.LoginVO;
import com.groupreport.platform.vo.UserVO;
import com.groupreport.platform.common.PageResult;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 登录响应（包含Token）
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 用户登出
     */
    void logout();

    /**
     * 获取当前登录用户信息
     * @return 用户信息
     */
    UserVO getCurrentUser();

    /**
     * 修改密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(String oldPassword, String newPassword);
}
