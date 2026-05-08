package com.cococlaw.tokenshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cococlaw.tokenshop.common.BusinessException;
import com.cococlaw.tokenshop.common.ResultCode;
import com.cococlaw.tokenshop.dto.ApiKeyResponse;
import com.cococlaw.tokenshop.dto.CreateApiKeyRequest;
import com.cococlaw.tokenshop.entity.ApiKey;
import com.cococlaw.tokenshop.mapper.ApiKeyMapper;
import com.cococlaw.tokenshop.service.ApiKeyService;
import com.cococlaw.tokenshop.utils.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API密钥服务实现
 */
@Slf4j
@Service
public class ApiKeyServiceImpl implements ApiKeyService {

    @Autowired
    private ApiKeyMapper apiKeyMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 创建API密钥
     */
    @Override
    @Transactional
    public ApiKeyResponse createApiKey(Long userId, CreateApiKeyRequest request) {
        // 生成新的API密钥
        String apiKey = IdUtil.generateApiKey();

        ApiKey key = new ApiKey();
        key.setUserId(userId);
        key.setApiKey(apiKey);
        key.setName(request.getName() != null ? request.getName() : "默认密钥");
        key.setStatus(1);
        key.setTotalRequests(0L);
        key.setTotalTokens(0L);

        apiKeyMapper.insert(key);

        log.info("用户{}创建了新的API密钥: {}", userId, apiKey.substring(0, 15) + "***");

        return ApiKeyResponse.builder()
                .id(key.getId())
                .apiKey(apiKey)
                .name(key.getName())
                .status(key.getStatus())
                .lastUsedTime(key.getLastUsedTime())
                .totalRequests(key.getTotalRequests())
                .totalTokens(key.getTotalTokens())
                .createTime(key.getCreateTime())
                .build();
    }

    /**
     * 获取用户的密钥列表
     */
    @Override
    public List<ApiKeyResponse> getApiKeyList(Long userId) {
        List<ApiKey> apiKeys = apiKeyMapper.selectList(
            new LambdaQueryWrapper<ApiKey>()
                .eq(ApiKey::getUserId, userId)
                .eq(ApiKey::getDeleted, 0)
                .orderByDesc(ApiKey::getCreateTime)
        );

        return apiKeys.stream()
                .map(key -> ApiKeyResponse.builder()
                        .id(key.getId())
                        .apiKey(key.getApiKey())
                        .name(key.getName())
                        .status(key.getStatus())
                        .lastUsedTime(key.getLastUsedTime())
                        .totalRequests(key.getTotalRequests())
                        .totalTokens(key.getTotalTokens())
                        .createTime(key.getCreateTime())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 删除API密钥
     */
    @Override
    @Transactional
    public void deleteApiKey(Long userId, Long id) {
        ApiKey apiKey = apiKeyMapper.selectOne(
            new LambdaQueryWrapper<ApiKey>()
                .eq(ApiKey::getId, id)
                .eq(ApiKey::getUserId, userId)
                .eq(ApiKey::getDeleted, 0)
        );

        if (apiKey == null) {
            throw new BusinessException(ResultCode.API_KEY_NOT_EXIST);
        }

        // 逻辑删除
        apiKeyMapper.update(null,
            new LambdaUpdateWrapper<ApiKey>()
                .eq(ApiKey::getId, id)
                .set(ApiKey::getDeleted, 1)
        );

        log.info("用户{}删除了API密钥: {}", userId, apiKey.getApiKey().substring(0, 15) + "***");
    }

    /**
     * 根据密钥验证并返回用户ID
     */
    @Override
    public Long validateApiKey(String apiKey) {
        ApiKey key = apiKeyMapper.selectOne(
            new LambdaQueryWrapper<ApiKey>()
                .eq(ApiKey::getApiKey, apiKey)
                .eq(ApiKey::getDeleted, 0)
        );

        if (key == null) {
            return null;
        }

        if (key.getStatus() == 0) {
            throw new BusinessException(ResultCode.API_KEY_DISABLED);
        }

        // 更新最后使用时间
        apiKeyMapper.update(null,
            new LambdaUpdateWrapper<ApiKey>()
                .eq(ApiKey::getId, key.getId())
                .set(ApiKey::getLastUsedTime, LocalDateTime.now())
        );

        return key.getUserId();
    }
}
