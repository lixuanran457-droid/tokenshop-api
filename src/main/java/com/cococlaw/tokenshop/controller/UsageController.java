package com.cococlaw.tokenshop.controller;

import com.cococlaw.tokenshop.common.Result;
import com.cococlaw.tokenshop.dto.UsageRecordResponse;
import com.cococlaw.tokenshop.dto.UsageStatsResponse;
import com.cococlaw.tokenshop.service.UsageService;
import com.cococlaw.tokenshop.utils.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 使用统计控制器
 */
@Api(tags = "使用统计接口")
@Slf4j
@RestController
@RequestMapping("/usage")
public class UsageController {

    @Autowired
    private UsageService usageService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取使用统计
     */
    @ApiOperation("获取使用统计")
    @GetMapping("/stats")
    public Result<UsageStatsResponse> getUsageStats(HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        UsageStatsResponse stats = usageService.getUsageStats(userId);
        return Result.success(stats);
    }

    /**
     * 获取消费记录
     */
    @ApiOperation("获取消费记录")
    @GetMapping("/records")
    public Result<List<UsageRecordResponse>> getUsageRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        List<UsageRecordResponse> records = usageService.getUsageRecords(userId, page, size);
        return Result.success(records);
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
