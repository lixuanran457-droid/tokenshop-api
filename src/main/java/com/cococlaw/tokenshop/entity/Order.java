package com.cococlaw.tokenshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("orders")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 套餐ID
     */
    private Long packageId;

    /**
     * 套餐名称
     */
    private String packageName;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 实际支付金额
     */
    private BigDecimal actualAmount;

    /**
     * 支付方式: wechat-微信, alipay-支付宝, card-信用卡
     */
    private String payType;

    /**
     * 支付状态: pending-待支付, paid-已支付, cancelled-已取消, refunded-已退款
     */
    private String payStatus;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 第三方交易号
     */
    private String tradeNo;

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
}
