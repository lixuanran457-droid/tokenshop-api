package com.cococlaw.tokenshop.service;

import com.cococlaw.tokenshop.dto.CreateOrderRequest;
import com.cococlaw.tokenshop.dto.OrderResponse;
import com.cococlaw.tokenshop.dto.PayResponse;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 创建订单
     */
    PayResponse createOrder(Long userId, CreateOrderRequest request);

    /**
     * 获取订单列表
     */
    List<OrderResponse> getOrderList(Long userId);

    /**
     * 获取订单详情
     */
    OrderResponse getOrderByNo(String orderNo);

    /**
     * 支付订单
     */
    PayResponse payOrder(String orderNo, String payType);

    /**
     * 处理支付回调
     */
    void handlePayCallback(String orderNo, String tradeNo, String payType);
}
