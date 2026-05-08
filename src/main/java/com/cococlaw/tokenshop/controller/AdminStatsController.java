package com.cococlaw.tokenshop.controller;

import com.cococlaw.tokenshop.common.Result;
import com.cococlaw.tokenshop.service.StatsService;
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
 * 管理员统计控制器
 */
@Api(tags = "管理统计")
@Slf4j
@RestController
@RequestMapping("/admin/stats")
public class AdminStatsController {

    @Autowired
    private StatsService statsService;

    /**
     * 获取仪表盘统计数据
     */
    @ApiOperation("获取仪表盘统计")
    @GetMapping
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = statsService.getDashboardStats();
        return Result.success(stats);
    }

    /**
     * 获取收入统计
     */
    @ApiOperation("获取收入统计")
    @GetMapping("/revenue")
    public Result<List<Map<String, Object>>> getRevenueStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> stats = statsService.getRevenueStats(startDate, endDate);
        return Result.success(stats);
    }

    /**
     * 获取用户统计
     */
    @ApiOperation("获取用户统计")
    @GetMapping("/users")
    public Result<List<Map<String, Object>>> getUserStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> stats = statsService.getUserStats(startDate, endDate);
        return Result.success(stats);
    }

    /**
     * 获取模型使用统计
     */
    @ApiOperation("获取模型使用统计")
    @GetMapping("/models")
    public Result<List<Map<String, Object>>> getModelStats() {
        List<Map<String, Object>> stats = statsService.getModelStats();
        return Result.success(stats);
    }
}
