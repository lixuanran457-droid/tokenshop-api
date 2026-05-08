package com.cococlaw.tokenshop.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 统计服务接口
 */
public interface StatsService {

    /**
     * 获取仪表盘统计数据
     */
    Map<String, Object> getDashboardStats();

    /**
     * 获取收入统计
     */
    List<Map<String, Object>> getRevenueStats(String startDate, String endDate);

    /**
     * 获取用户统计
     */
    List<Map<String, Object>> getUserStats(String startDate, String endDate);

    /**
     * 获取模型使用统计
     */
    List<Map<String, Object>> getModelStats();
}
