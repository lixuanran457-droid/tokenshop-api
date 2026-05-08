package com.cococlaw.tokenshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 使用统计响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageStatsResponse {
    
    /**
     * 今日请求数
     */
    private Long todayRequests;

    /**
     * 今日Token消耗
     */
    private Long todayTokens;

    /**
     * 本月请求数
     */
    private Long monthRequests;

    /**
     * 本月消费金额
     */
    private BigDecimal monthConsume;

    /**
     * 累计请求数
     */
    private Long totalRequests;

    /**
     * 累计Token消耗
     */
    private Long totalTokens;

    /**
     * 累计消费金额
     */
    private BigDecimal totalConsume;
}
