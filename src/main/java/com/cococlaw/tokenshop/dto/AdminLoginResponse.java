package com.cococlaw.tokenshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员登录响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginResponse {
    /**
     * 访问令牌
     */
    private String token;

    /**
     * 管理员ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 角色
     */
    private String role;

    /**
     * 过期时间(秒)
     */
    private Long expiresIn;
}
