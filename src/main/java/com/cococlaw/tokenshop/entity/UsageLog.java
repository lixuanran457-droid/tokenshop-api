package com.cococlaw.tokenshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 使用记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("usage_log")
public class UsageLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * API密钥ID
     */
    private Long apiKeyId;

    /**
     * 使用的API密钥
     */
    private String apiKey;

    /**
     * 调用的模型ID
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
     * 错误信息
     */
    private String errorMsg;

    /**
     * 请求IP
     */
    private String ip;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
