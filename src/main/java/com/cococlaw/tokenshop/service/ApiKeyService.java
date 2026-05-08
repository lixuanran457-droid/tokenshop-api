package com.cococlaw.tokenshop.service;

import com.cococlaw.tokenshop.dto.ApiKeyResponse;
import com.cococlaw.tokenshop.dto.CreateApiKeyRequest;

import java.util.List;

/**
 * API密钥服务接口
 */
public interface ApiKeyService {

    /**
     * 创建API密钥
     */
    ApiKeyResponse createApiKey(Long userId, CreateApiKeyRequest request);

    /**
     * 获取用户的密钥列表
     */
    List<ApiKeyResponse> getApiKeyList(Long userId);

    /**
     * 删除API密钥
     */
    void deleteApiKey(Long userId, Long id);

    /**
     * 根据密钥验证并返回用户ID
     */
    Long validateApiKey(String apiKey);
}
