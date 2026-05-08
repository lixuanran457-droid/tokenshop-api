package com.cococlaw.tokenshop.controller;

import com.cococlaw.tokenshop.common.BusinessException;
import com.cococlaw.tokenshop.common.Result;
import com.cococlaw.tokenshop.common.ResultCode;
import com.cococlaw.tokenshop.dto.AdminInfoResponse;
import com.cococlaw.tokenshop.dto.AdminLoginRequest;
import com.cococlaw.tokenshop.dto.AdminLoginResponse;
import com.cococlaw.tokenshop.service.AdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 管理员认证控制器
 */
@Api(tags = "管理员认证")
@Slf4j
@RestController
@RequestMapping("/admin/auth")
public class AdminAuthController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private com.cococlaw.tokenshop.utils.JwtUtil jwtUtil;

    /**
     * 管理员登录
     */
    @ApiOperation("管理员登录")
    @PostMapping("/login")
    public Result<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
        AdminLoginResponse response = adminService.login(request);
        return Result.success("登录成功", response);
    }

    /**
     * 获取当前管理员信息
     */
    @ApiOperation("获取管理员信息")
    @GetMapping("/info")
    public Result<AdminInfoResponse> getAdminInfo(HttpServletRequest request) {
        Long adminId = getAdminIdFromRequest(request);
        if (adminId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        AdminInfoResponse response = adminService.getAdminInfo(adminId);
        return Result.success(response);
    }

    /**
     * 退出登录
     */
    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        Long adminId = getAdminIdFromRequest(request);
        if (adminId != null) {
            adminService.logout(adminId);
        }
        return Result.success("退出成功", null);
    }

    /**
     * 修改密码
     */
    @ApiOperation("修改密码")
    @PostMapping("/password")
    public Result<Void> changePassword(
            HttpServletRequest request,
            @RequestBody Map<String, String> params) {
        // TODO: 实现修改密码逻辑
        return Result.success("密码修改成功", null);
    }

    /**
     * 从请求中获取管理员ID
     */
    private Long getAdminIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.isAdminToken(token)) {
                return jwtUtil.getAdminIdFromToken(token);
            }
        }
        return null;
    }
}
