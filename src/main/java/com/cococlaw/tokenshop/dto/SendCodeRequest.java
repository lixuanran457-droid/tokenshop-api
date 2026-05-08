package com.cococlaw.tokenshop.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 发送验证码请求
 */
@Data
public class SendCodeRequest {
    
    /**
     * 手机号或邮箱
     */
    @NotBlank(message = "手机号或邮箱不能为空")
    private String phoneOrEmail;

    /**
     * 验证码类型: login-登录, register-注册
     */
    @NotBlank(message = "类型不能为空")
    private String type;
}
