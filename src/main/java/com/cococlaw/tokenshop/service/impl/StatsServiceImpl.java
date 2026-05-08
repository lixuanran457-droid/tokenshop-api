package com.cococlaw.tokenshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cococlaw.tokenshop.entity.Order;
import com.cococlaw.tokenshop.entity.Transaction;
import com.cococlaw.tokenshop.entity.UsageLog;
import com.cococlaw.tokenshop.entity.User;
import com.cococlaw.tokenshop.entity.ApiKey;
import com.cococlaw.tokenshop.mapper.OrderMapper;
import com.cococlaw.tokenshop.mapper.TransactionMapper;
import com.cococlaw.tokenshop.mapper.UsageLogMapper;
import com.cococlaw.tokenshop.mapper.UserMapper;
import com.cococlaw.tokenshop.mapper.ApiKeyMapper;
import com.cococlaw.tokenshop.service.StatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务实现
 */
@Slf4j
@Service
public class StatsServiceImpl implements StatsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private UsageLogMapper usageLogMapper;

    @Autowired
    private ApiKeyMapper apiKeyMapper;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 用户总数
        Long totalUsers = userMapper.selectCount(null);
        stats.put("totalUsers", totalUsers);

        // 活跃用户数（今天有API调用的用户）
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        List<Long> activeUserIds = usageLogMapper.selectList(
            new LambdaQueryWrapper<UsageLog>()
                .select(UsageLog::getUserId)
                .ge(UsageLog::getCreateTime, todayStart)
                .isNotNull(UsageLog::getUserId)
                .groupBy(UsageLog::getUserId)
        ).stream().map(UsageLog::getUserId).filter(Objects::nonNull).collect(Collectors.toList());
        stats.put("activeUsers", activeUserIds.size());

        // 订单总数
        Long totalOrders = orderMapper.selectCount(null);
        stats.put("totalOrders", totalOrders);

        // 今日订单数
        Long todayOrders = orderMapper.selectCount(
            new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, todayStart)
        );
        stats.put("todayOrders", todayOrders);

        // 总收入
        BigDecimal totalRevenue = transactionMapper.selectSumAmount();
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        // 今日收入
        BigDecimal todayRevenue = transactionMapper.selectTodayAmount();
        stats.put("todayRevenue", todayRevenue != null ? todayRevenue : BigDecimal.ZERO);

        // API调用总数
        Long totalApiCalls = usageLogMapper.selectCount(null);
        stats.put("totalApiCalls", totalApiCalls);

        // 活跃API密钥数
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        Long activeApiKeys = apiKeyMapper.selectCount(
            new LambdaQueryWrapper<ApiKey>()
                .eq(ApiKey::getStatus, 1)
                .isNotNull(ApiKey::getLastUsedTime)
                .ge(ApiKey::getLastUsedTime, tenMinutesAgo)
        );
        stats.put("activeApiKeys", activeApiKeys);

        return stats;
    }

    @Override
    public List<Map<String, Object>> getRevenueStats(String startDate, String endDate) {
        List<Map<String, Object>> stats = new ArrayList<>();

        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(7);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

        // 生成日期范围内的所有日期
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            dates.add(current);
            current = current.plusDays(1);
        }

        // 获取这些日期的交易数据
        List<Transaction> transactions = transactionMapper.selectList(
            new LambdaQueryWrapper<Transaction>()
                .ge(Transaction::getCreateTime, start.atStartOfDay())
                .le(Transaction::getCreateTime, end.plusDays(1).atStartOfDay())
                .in(Transaction::getType, "recharge", "order")
        );

        // 按日期分组统计
        Map<LocalDate, BigDecimal> revenueByDate = transactions.stream()
            .collect(Collectors.groupingBy(
                t -> t.getCreateTime().toLocalDate(),
                Collectors.reducing(
                    BigDecimal.ZERO,
                    t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO,
                    BigDecimal::add
                )
            ));

        // 生成结果
        for (LocalDate date : dates) {
            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("date", date.format(DateTimeFormatter.ISO_DATE));
            dayStat.put("revenue", revenueByDate.getOrDefault(date, BigDecimal.ZERO));

            // 获取该日期的订单数
            Long orders = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                    .ge(Order::getCreateTime, date.atStartOfDay())
                    .lt(Order::getCreateTime, date.plusDays(1).atStartOfDay())
            );
            dayStat.put("orders", orders);

            stats.add(dayStat);
        }

        return stats;
    }

    @Override
    public List<Map<String, Object>> getUserStats(String startDate, String endDate) {
        List<Map<String, Object>> stats = new ArrayList<>();

        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(7);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

        // 生成日期范围内的所有日期
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            dates.add(current);
            current = current.plusDays(1);
        }

        // 获取这些日期的新用户数据
        List<User> newUsers = userMapper.selectList(
            new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, start.atStartOfDay())
                .le(User::getCreateTime, end.plusDays(1).atStartOfDay())
        );

        // 按日期分组统计新用户
        Map<LocalDate, Long> newUsersByDate = newUsers.stream()
            .collect(Collectors.groupingBy(
                u -> u.getCreateTime().toLocalDate(),
                Collectors.counting()
            ));

        // 获取活跃用户数据（今天有API调用的）
        for (LocalDate date : dates) {
            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("date", date.format(DateTimeFormatter.ISO_DATE));
            dayStat.put("newUsers", newUsersByDate.getOrDefault(date, 0L));

            // 活跃用户：当天有API调用的用户
            Long activeUsers = usageLogMapper.selectCount(
                new LambdaQueryWrapper<UsageLog>()
                    .ge(UsageLog::getCreateTime, date.atStartOfDay())
                    .lt(UsageLog::getCreateTime, date.plusDays(1).atStartOfDay())
                    .isNotNull(UsageLog::getUserId)
            );
            dayStat.put("activeUsers", activeUsers);

            stats.add(dayStat);
        }

        return stats;
    }

    @Override
    public List<Map<String, Object>> getModelStats() {
        List<Map<String, Object>> stats = new ArrayList<>();

        // 获取所有模型的使用统计
        List<UsageLog> usageLogs = usageLogMapper.selectList(null);

        // 按模型分组
        Map<String, List<UsageLog>> usageByModel = usageLogs.stream()
            .collect(Collectors.groupingBy(
                u -> u.getModel() != null ? u.getModel() : "unknown"
            ));

        for (Map.Entry<String, List<UsageLog>> entry : usageByModel.entrySet()) {
            Map<String, Object> modelStat = new HashMap<>();
            modelStat.put("model", entry.getKey());
            modelStat.put("totalCalls", entry.getValue().size());
            modelStat.put("totalTokens", entry.getValue().stream()
                .mapToLong(u -> (u.getInputTokens() != null ? u.getInputTokens() : 0) +
                              (u.getOutputTokens() != null ? u.getOutputTokens() : 0))
                .sum());
            modelStat.put("totalCost", entry.getValue().stream()
                .reduce(BigDecimal.ZERO,
                    (sum, u) -> sum.add(u.getCost() != null ? u.getCost() : BigDecimal.ZERO),
                    BigDecimal::add));

            stats.add(modelStat);
        }

        // 按调用数排序
        stats.sort((a, b) -> Long.compare(
            (Long) b.get("totalCalls"),
            (Long) a.get("totalCalls")
        ));

        return stats;
    }
}
