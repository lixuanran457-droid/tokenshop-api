package com.cococlaw.tokenshop.service;

import com.cococlaw.tokenshop.dto.UsageRecordResponse;
import com.cococlaw.tokenshop.dto.UsageStatsResponse;

import java.util.List;

/**
 * 使用统计服务接口
 */
public interface UsageService {

    /**
     * 获取使用统计
     */
    UsageStatsResponse getUsageStats(Long userId);

    /**
     * 获取使用记录
     */
    List<UsageRecordResponse> getUsageRecords(Long userId, int page, int size);

    /**
     * 记录使用
     */
    void recordUsage(Long userId, Long apiKeyId, String apiKey, String modelId, 
                     String modelName, int inputTokens, int outputTokens, 
                     java.math.BigDecimal cost, int latencyMs, int statusCode, 
                     String errorMsg, String ip);
}
