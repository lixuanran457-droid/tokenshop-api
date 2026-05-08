package com.cococlaw.tokenshop.controller;

import com.cococlaw.tokenshop.common.Result;
import com.cococlaw.tokenshop.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 支付控制器
 */
@Api(tags = "支付接口")
@Slf4j
@RestController
@RequestMapping("/pay")
public class PayController {

    @Autowired
    private OrderService orderService;

    /**
     * 微信支付回调
     */
    @ApiOperation("微信支付回调")
    @PostMapping("/callback/wechat")
    public String wechatCallback(@RequestBody Map<String, String> params) {
        log.info("收到微信支付回调: {}", params);
        
        try {
            String orderNo = params.get("out_trade_no");
            String transactionId = params.get("transaction_id");
            
            if (orderNo != null) {
                orderService.handlePayCallback(orderNo, transactionId, "wechat");
                return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
            }
        } catch (Exception e) {
            log.error("处理微信支付回调失败", e);
        }
        
        return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[ERROR]]></return_msg></xml>";
    }

    /**
     * 支付宝支付回调
     */
    @ApiOperation("支付宝支付回调")
    @PostMapping("/callback/alipay")
    public String alipayCallback(HttpServletRequest request) {
        log.info("收到支付宝支付回调");
        
        try {
            String orderNo = request.getParameter("out_trade_no");
            String tradeNo = request.getParameter("trade_no");
            String tradeStatus = request.getParameter("trade_status");
            
            // 支付宝回调需要验证签名，这里简化处理
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                if (orderNo != null) {
                    orderService.handlePayCallback(orderNo, tradeNo, "alipay");
                }
                return "success";
            }
        } catch (Exception e) {
            log.error("处理支付宝支付回调失败", e);
        }
        
        return "fail";
    }
}
