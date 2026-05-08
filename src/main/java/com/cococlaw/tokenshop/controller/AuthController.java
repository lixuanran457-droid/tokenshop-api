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
     * 统一登录接口
     * 支持密码登录和验证码登录两种方式
     */
    @ApiOperation("用户登录（支持密码/验证码两种方式）")
    @PostMapping("/login")
    public Result<AuthResponse> login(@RequestBody @Validated LoginRequest request) {
        AuthResponse response = authService.login(request);
        return Result.success(response);
    }

    /**
     * 注册
     * 注册时需要密码
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<AuthResponse> register(@RequestBody @Validated RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return Result.success(response);
    }
}
