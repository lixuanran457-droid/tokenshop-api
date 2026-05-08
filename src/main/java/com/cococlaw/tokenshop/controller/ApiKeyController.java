package com.cococlaw.tokenshop.controller;

import com.cococlaw.tokenshop.common.Result;
import com.cococlaw.tokenshop.dto.ApiKeyResponse;
import com.cococlaw.tokenshop.dto.CreateApiKeyRequest;
import com.cococlaw.tokenshop.service.ApiKeyService;
import com.cococlaw.tokenshop.utils.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * API密钥控制器
 */
@Api(tags = "API密钥接口")
@Slf4j
@RestController
@RequestMapping("/keys")
public class ApiKeyController {

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 创建API密钥
     */
    @ApiOperation("创建API密钥")
    @PostMapping
    public Result<ApiKeyResponse> createApiKey(
            @RequestBody @Valid CreateApiKeyRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        ApiKeyResponse response = apiKeyService.createApiKey(userId, request);
        return Result.success("API密钥创建成功", response);
    }

    /**
     * 获取密钥列表
     */
    @ApiOperation("获取密钥列表")
    @GetMapping
    public Result<List<ApiKeyResponse>> getApiKeyList(HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        List<ApiKeyResponse> keys = apiKeyService.getApiKeyList(userId);
        return Result.success(keys);
    }

    /**
     * 删除密钥
     */
    @ApiOperation("删除API密钥")
    @DeleteMapping("/{id}")
    public Result<Void> deleteApiKey(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        apiKeyService.deleteApiKey(userId, id);
        return Result.success("API密钥删除成功");
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
