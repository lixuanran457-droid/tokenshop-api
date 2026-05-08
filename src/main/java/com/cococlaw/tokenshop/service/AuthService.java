package com.cococlaw.tokenshop.service;

import com.cococlaw.tokenshop.dto.*;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 发送验证码
     */
    void sendCode(String phoneOrEmail, String type);

    /**
     * 手机号+验证码登录
     */
    AuthResponse phoneLogin(PhoneLoginRequest request);

    /**
     * 邮箱+密码登录
     */
    AuthResponse emailLogin(EmailLoginRequest request);

    /**
     * 注册
     */
    AuthResponse register(RegisterRequest request);
}
