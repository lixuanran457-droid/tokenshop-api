package com.cococlaw.tokenshop.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 创建订单请求
 */
@Data
public class CreateOrderRequest {
    
    /**
     * 套餐ID
     */
    @NotNull(message = "套餐ID不能为空")
    private Long packageId;

    /**
     * 支付方式: wechat-微信, alipay-支付宝, card-信用卡
     */
    @NotBlank(message = "支付方式不能为空")
    private String payType;
}
