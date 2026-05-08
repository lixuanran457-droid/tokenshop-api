package com.cococlaw.tokenshop.controller;

import com.cococlaw.tokenshop.common.Result;
import com.cococlaw.tokenshop.entity.SysConfig;
import com.cococlaw.tokenshop.service.SysConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 */
@Api(tags = "系统配置")
@Slf4j
@RestController
@RequestMapping("/config")
public class SysConfigController {

    @Autowired
    private SysConfigService sysConfigService;

    /**
     * 获取所有配置（按分组）
     */
    @ApiOperation("获取所有配置")
    @GetMapping("/all")
    public Result<Map<String, List<SysConfig>>> getAllConfigsGrouped() {
        return Result.success(sysConfigService.getAllConfigsGrouped());
    }

    /**
     * 获取指定分组的配置
     */
    @ApiOperation("获取指定分组的配置")
    @GetMapping("/group/{groupKey}")
    public Result<List<SysConfig>> getConfigsByGroup(@PathVariable String groupKey) {
        return Result.success(sysConfigService.getConfigsByGroup(groupKey));
    }

    /**
     * 获取单个配置值
     */
    @ApiOperation("获取单个配置值")
    @GetMapping("/value/{key}")
    public Result<String> getConfigValue(@PathVariable String key) {
        String value = sysConfigService.getValue(key);
        return Result.success(value);
    }

    /**
     * 更新配置值
     */
    @ApiOperation("更新配置值")
    @PutMapping("/value/{key}")
    public Result<Boolean> updateConfigValue(
            @PathVariable String key,
            @RequestParam String value) {
        boolean success = sysConfigService.updateValue(key, value);
        return Result.success(success);
    }

    /**
     * 批量更新配置
     */
    @ApiOperation("批量更新配置")
    @PutMapping("/batch")
    public Result<Boolean> batchUpdateConfigs(@RequestBody List<SysConfig> configs) {
        boolean success = sysConfigService.batchUpdateConfigs(configs);
        return Result.success(success);
    }

    // ==================== 便捷方法 ====================

    /**
     * 获取网站基础配置
     */
    @ApiOperation("获取网站基础配置")
    @GetMapping("/site")
    public Result<Map<String, Object>> getSiteConfig() {
        Map<String, Object> config = Map.of(
                "siteName", sysConfigService.getValue("SITE_NAME", "TokenShop"),
                "siteSlogan", sysConfigService.getValue("SITE_SLOGAN", ""),
                "siteLogo", sysConfigService.getValue("SITE_LOGO", ""),
                "customerServiceEmail", sysConfigService.getValue("CUSTOMER_SERVICE_EMAIL", "")
        );
        return Result.success(config);
    }

    /**
     * 获取用户相关配置
     */
    @ApiOperation("获取用户相关配置")
    @GetMapping("/user")
    public Result<Map<String, Object>> getUserConfig() {
        Map<String, Object> config = Map.of(
                "newUserBonus", sysConfigService.getNumberValue("NEW_USER_BONUS"),
                "freeTokenQuota", sysConfigService.getNumberValue("FREE_TOKEN_QUOTA", java.math.BigDecimal.ZERO),
                "minRechargeAmount", sysConfigService.getNumberValue("MIN_RECHARGE_AMOUNT"),
                "maxRechargeAmount", sysConfigService.getNumberValue("MAX_RECHARGE_AMOUNT")
        );
        return Result.success(config);
    }

    /**
     * 获取API相关配置
     */
    @ApiOperation("获取API相关配置")
    @GetMapping("/api")
    public Result<Map<String, Object>> getApiConfig() {
        Map<String, Object> config = Map.of(
                "apiBaseUrl", sysConfigService.getValue("API_BASE_URL", ""),
                "apiTimeout", sysConfigService.getNumberValue("API_TIMEOUT"),
                "defaultModel", sysConfigService.getValue("DEFAULT_MODEL", "gpt-3.5-turbo")
        );
        return Result.success(config);
    }

    /**
     * 获取系统状态
     */
    @ApiOperation("获取系统状态")
    @GetMapping("/status")
    public Result<Map<String, Object>> getSystemStatus() {
        Map<String, Object> status = Map.of(
                "maintenanceMode", sysConfigService.getBooleanValue("MAINTENANCE_MODE", false),
                "registrationEnabled", sysConfigService.getBooleanValue("REGISTRATION_ENABLED", true),
                "wechatEnabled", sysConfigService.getBooleanValue("WECHAT_ENABLED", false),
                "alipayEnabled", sysConfigService.getBooleanValue("ALIPAY_ENABLED", false)
        );
        return Result.success(status);
    }
}
