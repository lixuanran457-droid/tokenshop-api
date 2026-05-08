package com.cococlaw.tokenshop.service;

import com.cococlaw.tokenshop.dto.AdminLoginRequest;
import com.cococlaw.tokenshop.dto.AdminLoginResponse;
import com.cococlaw.tokenshop.dto.AdminInfoResponse;

/**
 * 管理员服务接口
 */
public interface AdminService {

    /**
     * 管理员登录
     */
    AdminLoginResponse login(AdminLoginRequest request);

    /**
     * 获取管理员信息
     */
    AdminInfoResponse getAdminInfo(Long adminId);

    /**
     * 退出登录
     */
    void logout(Long adminId);
}
