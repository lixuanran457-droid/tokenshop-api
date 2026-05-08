package com.cococlaw.tokenshop.controller;

import com.cococlaw.tokenshop.common.Result;
import com.cococlaw.tokenshop.dto.CreateOrderRequest;
import com.cococlaw.tokenshop.dto.OrderResponse;
import com.cococlaw.tokenshop.dto.PayResponse;
import com.cococlaw.tokenshop.service.OrderService;
import com.cococlaw.tokenshop.utils.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 订单控制器
 */
@Api(tags = "订单接口")
@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 创建订单
     */
    @ApiOperation("创建订单")
    @PostMapping
    public Result<PayResponse> createOrder(
            @RequestBody @Validated CreateOrderRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        PayResponse response = orderService.createOrder(userId, request);
        return Result.success("订单创建成功", response);
    }

    /**
     * 获取订单列表
     */
    @ApiOperation("获取订单列表")
    @GetMapping
    public Result<List<OrderResponse>> getOrderList(HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        List<OrderResponse> orders = orderService.getOrderList(userId);
        return Result.success(orders);
    }

    /**
     * 获取订单详情
     */
    @ApiOperation("获取订单详情")
    @GetMapping("/{orderNo}")
    public Result<OrderResponse> getOrderDetail(@PathVariable String orderNo) {
        OrderResponse order = orderService.getOrderByNo(orderNo);
        return Result.success(order);
    }

    /**
     * 支付订单
     */
    @ApiOperation("支付订单")
    @PostMapping("/pay")
    public Result<PayResponse> payOrder(
            @RequestParam String orderNo,
            @RequestParam String payType) {
        PayResponse response = orderService.payOrder(orderNo, payType);
        return Result.success(response);
    }

    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        return null;
    }
}
