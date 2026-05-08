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
 * 套餐实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("package")
public class Package implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 套餐ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 套餐名称
     */
    private String name;

    /**
     * 套餐描述
     */
    private String description;

    /**
     * 价格(元)
     */
    private BigDecimal price;

    /**
     * 包含Token数量(为0表示按实际用量计费)
     */
    private Long tokens;

    /**
     * 有效期(天)
     */
    private Integer validityDays;

    /**
     * 赠送金额
     */
    private BigDecimal bonus;

    /**
     * 是否推荐
     */
    private Integer isPopular;

    /**
     * 状态: 0-下架, 1-上架
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
