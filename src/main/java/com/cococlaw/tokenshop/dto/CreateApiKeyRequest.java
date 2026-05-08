package com.cococlaw.tokenshop.dto;

import lombok.Data;

/**
 * 创建API密钥请求
 */
@Data
public class CreateApiKeyRequest {
    
    /**
     * 密钥名称
     */
    private String name;
}
