package com.cococlaw.tokenshop.controller;

import com.cococlaw.tokenshop.common.Result;
import com.cococlaw.tokenshop.dto.*;
import com.cococlaw.tokenshop.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 认证控制器
 */
@Api(tags = "认证接口")
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 发送验证码
     */
    @ApiOperation("发送验证码")
    @PostMapping("/sendCode")
    public Result<Void> sendCode(@RequestBody @Validated SendCodeRequest request) {
        authService.sendCode(request.getPhoneOrEmail(), request.getType());
        return Result.success("验证码发送成功");
    }

    /**
     * 手机号登录
     */
    @ApiOperation("手机号登录")
    @PostMapping("/login/phone")
    public Result<AuthResponse> phoneLogin(@RequestBody @Validated PhoneLoginRequest request) {
        AuthResponse response = authService.phoneLogin(request);
        return Result.success(response);
    }

    /**
     * 邮箱登录
     */
    @ApiOperation("邮箱登录")
    @PostMapping("/login/email")
    public Result<AuthResponse> emailLogin(@RequestBody @Validated EmailLoginRequest request) {
        AuthResponse response = authService.emailLogin(request);
        return Result.success(response);
    }

    /**
     * 注册
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<AuthResponse> register(@RequestBody @Validated RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return Result.success(response);
    }
}
