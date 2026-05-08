package com.cococlaw.tokenshop.dto;

import lombok.Data;

/**
 * 管理员登录请求
 */
@Data
public class AdminLoginRequest {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
