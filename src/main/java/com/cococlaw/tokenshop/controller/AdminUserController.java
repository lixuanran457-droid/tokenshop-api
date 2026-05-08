package com.cococlaw.tokenshop.controller;

import com.cococlaw.tokenshop.common.BusinessException;
import com.cococlaw.tokenshop.common.Result;
import com.cococlaw.tokenshop.common.ResultCode;
import com.cococlaw.tokenshop.entity.User;
import com.cococlaw.tokenshop.service.UserService;
import com.cococlaw.tokenshop.utils.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 管理员用户管理控制器
 */
@Api(tags = "管理-用户")
@Slf4j
@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取用户列表
     */
    @ApiOperation("获取用户列表")
    @GetMapping
    public Result<List<User>> getUserList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        List<User> users = userService.getUserList(keyword, page, pageSize);
        return Result.success(users);
    }

    /**
     * 获取用户总数
     */
    @ApiOperation("获取用户总数")
    @GetMapping("/count")
    public Result<Long> getUserCount(@RequestParam(required = false) String keyword) {
        Long count = userService.getUserCount(keyword);
        return Result.success(count);
    }

    /**
     * 获取用户详情
     */
    @ApiOperation("获取用户详情")
    @GetMapping("/{id}")
    public Result<User> getUserDetail(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        // 清除敏感信息
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 禁用用户
     */
    @ApiOperation("禁用用户")
    @PostMapping("/{id}/disable")
    public Result<Void> disableUser(@PathVariable Long id) {
        userService.disableUser(id);
        return Result.success("禁用成功", null);
    }

    /**
     * 启用用户
     */
    @ApiOperation("启用用户")
    @PostMapping("/{id}/enable")
    public Result<Void> enableUser(@PathVariable Long id) {
        userService.enableUser(id);
        return Result.success("启用成功", null);
    }

    /**
     * 调整用户余额
     */
    @ApiOperation("调整用户余额")
    @PostMapping("/{id}/balance")
    public Result<Void> adjustBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false, defaultValue = "管理员调整") String reason) {
        userService.adjustBalance(id, amount, reason);
        return Result.success("余额调整成功", null);
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
