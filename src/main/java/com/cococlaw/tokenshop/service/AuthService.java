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
     * 统一登录接口
     * 支持密码登录和验证码登录两种方式
     */
    AuthResponse login(LoginRequest request);

    /**
     * 注册
     * 注册时需要密码
     */
    AuthResponse register(RegisterRequest request);
}
