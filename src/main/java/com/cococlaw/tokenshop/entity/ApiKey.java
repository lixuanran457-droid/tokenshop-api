package com.cococlaw.tokenshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * API密钥实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("api_key")
public class ApiKey implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 密钥ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除: 0-未删除, 1-已删除
     */
    @TableLogic
    private Integer deleted;
}
