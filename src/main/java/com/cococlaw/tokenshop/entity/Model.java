package com.cococlaw.tokenshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模型实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("model")
public class Model implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模型ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模型标识符
     */
    private String modelId;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型厂商
     */
    private String provider;

    /**
     * 厂商全称
     */
    private String providerName;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 输入价格(元/1K tokens)
     */
    private BigDecimal inputPrice;

    /**
     * 输出价格(元/1K tokens)
     */
    private BigDecimal outputPrice;

    /**
     * 上下文窗口大小
     */
    private Integer contextWindow;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
