package com.cococlaw.tokenshop.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 注册请求
 */
@Data
public class RegisterRequest {
    
    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度应为6-32位")
    private String password;

    /**
     * 验证码
     */
    private String code;

    /**
     * 注册方式: phone-手机号, email-邮箱
     */
    @NotBlank(message = "注册方式不能为空")
    private String registerType;
}
