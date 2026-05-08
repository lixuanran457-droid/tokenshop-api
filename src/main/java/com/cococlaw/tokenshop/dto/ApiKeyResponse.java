package com.cococlaw.tokenshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API密钥响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyResponse {
    
    /**
     * 密钥ID
     */
    private Long id;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 密钥名称
     */
    private String name;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedTime;

    /**
     * 累计请求次数
     */
    private Long totalRequests;

    /**
     * 累计消耗Token数
     */
    private Long totalTokens;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
