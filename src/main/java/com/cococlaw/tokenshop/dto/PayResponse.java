package com.cococlaw.tokenshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayResponse {
    
    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 支付二维码链接(微信/支付宝)
     */
    private String qrCode;

    /**
     * 支付跳转URL
     */
    private String payUrl;

    /**
     * 支付页面HTML(用于跳转)
     */
    private String payHtml;
}
