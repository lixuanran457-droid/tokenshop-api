package com.cococlaw.tokenshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户信息响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 账户余额
     */
    private BigDecimal balance;

    /**
     * 累计充值金额
     */
    private BigDecimal totalRecharge;

    /**
     * 累计消费金额
     */
    private BigDecimal totalConsume;

    /**
     * 注册时间
     */
    private LocalDateTime createTime;
}
