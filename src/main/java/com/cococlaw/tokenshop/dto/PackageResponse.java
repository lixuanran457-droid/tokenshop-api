package com.cococlaw.tokenshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 套餐响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageResponse {
    
    /**
     * 套餐ID
     */
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
     * 包含Token数量
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
}
