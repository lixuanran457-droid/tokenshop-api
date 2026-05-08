package com.cococlaw.tokenshop.service;

import com.cococlaw.tokenshop.entity.SysConfig;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 系统配置服务接口
 */
public interface SysConfigService {

    /**
     * 获取配置值
     * @param key 配置键
     * @return 配置值
     */
    String getValue(String key);

    /**
     * 获取配置值，如果为空则返回默认值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    String getValue(String key, String defaultValue);

    /**
     * 获取数值类型配置
     * @param key 配置键
     * @return 配置值
     */
    BigDecimal getNumberValue(String key);

    /**
     * 获取数值类型配置，如果为空则返回默认值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    BigDecimal getNumberValue(String key, BigDecimal defaultValue);

    /**
     * 获取布尔类型配置
     * @param key 配置键
     * @return 配置值
     */
    Boolean getBooleanValue(String key);

    /**
     * 获取布尔类型配置，如果为空则返回默认值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    Boolean getBooleanValue(String key, Boolean defaultValue);

    /**
     * 获取所有配置（按分组）
     * @return 分组后的配置
     */
    Map<String, List<SysConfig>> getAllConfigsGrouped();

    /**
     * 获取所有配置列表
     * @return 配置列表
     */
    List<SysConfig> getAllConfigs();

    /**
     * 获取指定分组的配置
     * @param groupKey 分组键
     * @return 配置列表
     */
    List<SysConfig> getConfigsByGroup(String groupKey);

    /**
     * 更新配置值
     * @param key 配置键
     * @param value 配置值
     * @return 是否成功
     */
    boolean updateValue(String key, String value);

    /**
     * 批量更新配置
     * @param configs 配置列表
     * @return 是否成功
     */
    boolean batchUpdateConfigs(List<SysConfig> configs);
}
