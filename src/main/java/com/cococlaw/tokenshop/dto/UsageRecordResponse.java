package com.cococlaw.tokenshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 使用记录响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageRecordResponse {
    
    /**
     * 记录ID
     */
    private Long id;

    /**
     * 模型ID
     */
    private String modelId;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 输入Token数
     */
    private Integer inputTokens;

    /**
     * 输出Token数
     */
    private Integer outputTokens;

    /**
     * 总Token数
     */
    private Integer totalTokens;

    /**
     * 消费金额
     */
    private BigDecimal cost;

    /**
     * 响应延迟(毫秒)
     */
    private Integer latencyMs;

    /**
     * 状态码
     */
    private Integer statusCode;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
