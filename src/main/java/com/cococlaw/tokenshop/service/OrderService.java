package com.cococlaw.tokenshop.service;

import com.cococlaw.tokenshop.dto.CreateOrderRequest;
import com.cococlaw.tokenshop.dto.OrderResponse;
import com.cococlaw.tokenshop.dto.PayResponse;
import com.cococlaw.tokenshop.entity.Order;

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
     * 获取订单实体
     */
    Order getOrderByNoEntity(String orderNo);

    /**
     * 支付订单
     */
    PayResponse payOrder(String orderNo, String payType);

    /**
     * 处理支付回调
     */
    void handlePayCallback(String orderNo, String tradeNo, String payType);

    // ==================== 管理员方法 ====================

    /**
     * 获取订单列表（管理员）
     */
    List<Order> getAdminOrderList(String status, String keyword, Integer page, Integer pageSize);

    /**
     * 获取订单总数（管理员）
     */
    Long getOrderCount(String status);

    /**
     * 取消订单
     */
    void cancelOrder(String orderNo);

    /**
     * 删除订单
     */
    void deleteOrder(String orderNo);
}
