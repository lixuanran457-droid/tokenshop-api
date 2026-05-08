package com.cococlaw.tokenshop.controller;

import com.cococlaw.tokenshop.common.Result;
import com.cococlaw.tokenshop.dto.UserInfoResponse;
import com.cococlaw.tokenshop.service.UserService;
import com.cococlaw.tokenshop.utils.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户控制器
 */
@Api(tags = "用户接口")
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取用户信息
     */
    @ApiOperation("获取用户信息")
    @GetMapping("/info")
    public Result<UserInfoResponse> getUserInfo(HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        UserInfoResponse userInfo = userService.getUserInfo(userId);
        return Result.success(userInfo);
    }

    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        return null;
    }
}
