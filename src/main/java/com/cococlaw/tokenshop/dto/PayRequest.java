package com.cococlaw.tokenshop.dto;

import lombok.Data;

/**
 * 支付请求
 */
@Data
public class PayRequest {
    
    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 支付方式: wechat-微信, alipay-支付宝
     */
    private String payType;
}
