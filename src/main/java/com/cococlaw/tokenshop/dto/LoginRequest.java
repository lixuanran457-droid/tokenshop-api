package com.cococlaw.tokenshop.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 统一登录请求
 * 支持两种登录方式：
 * 1. 密码登录：账号 + 密码
 * 2. 验证码登录：账号 + 验证码
 * 两种方式二选一
 */
@Data
public class LoginRequest {
    
    /**
     * 账号（手机号或邮箱）
     */
    @NotBlank(message = "账号不能为空")
    private String account;

    /**
     * 登录方式: password-密码登录, code-验证码登录
     */
    @NotBlank(message = "登录方式不能为空")
    private String loginType;

    /**
     * 密码（密码登录时必填）
     */
    @Size(min = 6, max = 32, message = "密码长度应为6-32位")
    private String password;

    /**
     * 验证码（验证码登录时必填）
     */
    private String code;

    /**
     * 账号类型: phone-手机号, email-邮箱
     */
    @NotBlank(message = "账号类型不能为空")
    private String accountType;
}
