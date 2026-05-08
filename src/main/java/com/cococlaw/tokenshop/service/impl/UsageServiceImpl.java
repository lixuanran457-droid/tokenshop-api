package com.cococlaw.tokenshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cococlaw.tokenshop.dto.UsageRecordResponse;
import com.cococlaw.tokenshop.dto.UsageStatsResponse;
import com.cococlaw.tokenshop.entity.ApiKey;
import com.cococlaw.tokenshop.entity.UsageLog;
import com.cococlaw.tokenshop.mapper.ApiKeyMapper;
import com.cococlaw.tokenshop.mapper.UsageLogMapper;
import com.cococlaw.tokenshop.service.UsageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 使用统计服务实现
 */
@Slf4j
@Service
public class UsageServiceImpl implements UsageService {

    @Autowired
    private UsageLogMapper usageLogMapper;

    @Autowired
    private ApiKeyMapper apiKeyMapper;

    /**
     * 获取使用统计
     */
    @Override
    public UsageStatsResponse getUsageStats(Long userId) {
        LocalDateTime today = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrow = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        // 今日请求数
        Long todayRequests = usageLogMapper.selectCount(
            new LambdaQueryWrapper<UsageLog>()
                .eq(UsageLog::getUserId, userId)
                .eq(UsageLog::getStatusCode, 200)
                .between(UsageLog::getCreateTime, today, tomorrow)
        );

        // 今日Token消耗
        Long todayTokens = usageLogMapper.selectObjs(
            new LambdaQueryWrapper<UsageLog>()
                .eq(UsageLog::getUserId, userId)
                .between(UsageLog::getCreateTime, today, tomorrow)
                .select(UsageLog::getTotalTokens)
        ).stream()
            .filter(obj -> obj != null)
            .mapToLong(obj -> ((Number) obj).longValue())
            .sum();

        // 本月请求数
        Long monthRequests = usageLogMapper.selectCount(
            new LambdaQueryWrapper<UsageLog>()
                .eq(UsageLog::getUserId, userId)
                .eq(UsageLog::getStatusCode, 200)
                .ge(UsageLog::getCreateTime, monthStart)
        );

        // 本月消费金额 (需要根据模型价格计算，这里简化处理)
        BigDecimal monthConsume = BigDecimal.ZERO;

        // 累计请求数
        Long totalRequests = usageLogMapper.selectCount(
            new LambdaQueryWrapper<UsageLog>()
                .eq(UsageLog::getUserId, userId)
                .eq(UsageLog::getStatusCode, 200)
        );

        // 累计Token消耗
        Long totalTokens = usageLogMapper.selectObjs(
            new LambdaQueryWrapper<UsageLog>()
                .eq(UsageLog::getUserId, userId)
                .select(UsageLog::getTotalTokens)
        ).stream()
            .filter(obj -> obj != null)
            .mapToLong(obj -> ((Number) obj).longValue())
            .sum();

        // 累计消费金额
        BigDecimal totalConsume = BigDecimal.ZERO;

        return UsageStatsResponse.builder()
                .todayRequests(todayRequests)
                .todayTokens(todayTokens)
                .monthRequests(monthRequests)
                .monthConsume(monthConsume)
                .totalRequests(totalRequests)
                .totalTokens(totalTokens)
                .totalConsume(totalConsume)
                .build();
    }

    /**
     * 获取使用记录
     */
    @Override
    public List<UsageRecordResponse> getUsageRecords(Long userId, int page, int size) {
        Page<UsageLog> pageParam = new Page<>(page, size);
        Page<UsageLog> result = usageLogMapper.selectPage(pageParam,
            new LambdaQueryWrapper<UsageLog>()
                .eq(UsageLog::getUserId, userId)
                .orderByDesc(UsageLog::getCreateTime)
        );

        return result.getRecords().stream()
                .map(log -> UsageRecordResponse.builder()
                        .id(log.getId())
                        .modelId(log.getModelId())
                        .modelName(log.getModelName())
                        .inputTokens(log.getInputTokens())
                        .outputTokens(log.getOutputTokens())
                        .totalTokens(log.getTotalTokens())
                        .cost(log.getCost())
                        .latencyMs(log.getLatencyMs())
                        .statusCode(log.getStatusCode())
                        .createTime(log.getCreateTime())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 记录使用
     */
    @Override
    @Transactional
    public void recordUsage(Long userId, Long apiKeyId, String apiKey, String modelId,
                            String modelName, int inputTokens, int outputTokens,
                            BigDecimal cost, int latencyMs, int statusCode,
                            String errorMsg, String ip) {
        // 保存使用记录
        UsageLog usageLog = new UsageLog();
        usageLog.setUserId(userId);
        usageLog.setApiKeyId(apiKeyId);
        usageLog.setApiKey(apiKey);
        usageLog.setModelId(modelId);
        usageLog.setModelName(modelName);
        usageLog.setInputTokens(inputTokens);
        usageLog.setOutputTokens(outputTokens);
        usageLog.setTotalTokens(inputTokens + outputTokens);
        usageLog.setCost(cost);
        usageLog.setLatencyMs(latencyMs);
        usageLog.setStatusCode(statusCode);
        usageLog.setErrorMsg(errorMsg);
        usageLog.setIp(ip);
        usageLogMapper.insert(usageLog);

        // 更新API密钥统计
        apiKeyMapper.update(null,
            new LambdaUpdateWrapper<ApiKey>()
                .eq(ApiKey::getId, apiKeyId)
                .set(ApiKey::getTotalRequests, apiKeyMapper.selectById(apiKeyId).getTotalRequests() + 1)
                .set(ApiKey::getTotalTokens, apiKeyMapper.selectById(apiKeyId).getTotalTokens() + inputTokens + outputTokens)
                .set(ApiKey::getLastUsedTime, LocalDateTime.now())
        );

        log.debug("记录API使用: 用户={}, 模型={}, Token={}, 费用={}", 
            userId, modelId, inputTokens + outputTokens, cost);
    }
}
