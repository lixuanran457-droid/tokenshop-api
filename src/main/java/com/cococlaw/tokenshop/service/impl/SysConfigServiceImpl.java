package com.cococlaw.tokenshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cococlaw.tokenshop.entity.SysConfig;
import com.cococlaw.tokenshop.mapper.SysConfigMapper;
import com.cococlaw.tokenshop.service.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 系统配置服务实现
 */
@Slf4j
@Service
public class SysConfigServiceImpl implements SysConfigService {

    @Autowired
    private SysConfigMapper sysConfigMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis缓存键前缀
     */
    private static final String CACHE_KEY_PREFIX = "sys_config:";

    /**
     * 缓存过期时间（分钟）
     */
    private static final long CACHE_EXPIRE_MINUTES = 30;

    @Override
    public String getValue(String key) {
        return getValue(key, null);
    }

    @Override
    public String getValue(String key, String defaultValue) {
        // 先从Redis缓存获取
        String cacheKey = CACHE_KEY_PREFIX + key;
        String cachedValue = (String) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedValue != null) {
            return cachedValue;
        }

        // 从数据库查询
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, key)
               .eq(SysConfig::getStatus, 1);
        SysConfig config = sysConfigMapper.selectOne(wrapper);

        if (config != null && config.getConfigValue() != null) {
            // 写入缓存
            redisTemplate.opsForValue().set(cacheKey, config.getConfigValue(), CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            return config.getConfigValue();
        }

        return defaultValue;
    }

    @Override
    public BigDecimal getNumberValue(String key) {
        return getNumberValue(key, null);
    }

    @Override
    public BigDecimal getNumberValue(String key, BigDecimal defaultValue) {
        String value = getValue(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            log.warn("配置项 {} 的值不是有效的数字: {}", key, value);
            return defaultValue;
        }
    }

    @Override
    public Boolean getBooleanValue(String key) {
        return getBooleanValue(key, null);
    }

    @Override
    public Boolean getBooleanValue(String key, Boolean defaultValue) {
        String value = getValue(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    @Override
    public Map<String, List<SysConfig>> getAllConfigsGrouped() {
        List<SysConfig> configs = getAllConfigs();
        return configs.stream()
                .collect(Collectors.groupingBy(
                        SysConfig::getGroupKey,
                        Collectors.toList()
                ));
    }

    @Override
    public List<SysConfig> getAllConfigs() {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getStatus, 1)
               .orderByAsc(SysConfig::getSortOrder);
        return sysConfigMapper.selectList(wrapper);
    }

    @Override
    public List<SysConfig> getConfigsByGroup(String groupKey) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getGroupKey, groupKey)
               .eq(SysConfig::getStatus, 1)
               .orderByAsc(SysConfig::getSortOrder);
        return sysConfigMapper.selectList(wrapper);
    }

    @Override
    public boolean updateValue(String key, String value) {
        // 更新数据库
        SysConfig config = new SysConfig();
        config.setConfigValue(value);
        
        int result = sysConfigMapper.update(config,
                new LambdaQueryWrapper<SysConfig>()
                        .eq(SysConfig::getConfigKey, key));

        // 清除缓存
        String cacheKey = CACHE_KEY_PREFIX + key;
        redisTemplate.delete(cacheKey);

        return result > 0;
    }

    @Override
    public boolean batchUpdateConfigs(List<SysConfig> configs) {
        for (SysConfig config : configs) {
            if (config.getConfigKey() != null && config.getConfigValue() != null) {
                updateValue(config.getConfigKey(), config.getConfigValue());
            }
        }
        return true;
    }
}
