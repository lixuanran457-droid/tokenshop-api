package com.cococlaw.tokenshop.controller;

import com.cococlaw.tokenshop.common.BusinessException;
import com.cococlaw.tokenshop.common.Result;
import com.cococlaw.tokenshop.common.ResultCode;
import com.cococlaw.tokenshop.dto.OrderResponse;
import com.cococlaw.tokenshop.entity.Order;
import com.cococlaw.tokenshop.mapper.OrderMapper;
import com.cococlaw.tokenshop.service.OrderService;
import com.cococlaw.tokenshop.utils.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 管理员订单管理控制器
 */
@Api(tags = "管理-订单")
@Slf4j
@RestController
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取订单列表
     */
    @ApiOperation("获取订单列表")
    @GetMapping
    public Result<List<Order>> getOrderList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        List<Order> orders = orderService.getAdminOrderList(status, keyword, page, pageSize);
        return Result.success(orders);
    }

    /**
     * 获取订单详情
     */
    @ApiOperation("获取订单详情")
    @GetMapping("/{orderNo}")
    public Result<Order> getOrderDetail(@PathVariable String orderNo) {
        Order order = orderService.getOrderByNo(orderNo);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        return Result.success(order);
    }

    /**
     * 取消订单
     */
    @ApiOperation("取消订单")
    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancelOrder(@PathVariable String orderNo) {
        orderService.cancelOrder(orderNo);
        return Result.success("订单已取消", null);
    }

    /**
     * 删除订单
     */
    @ApiOperation("删除订单")
    @DeleteMapping("/{orderNo}")
    public Result<Void> deleteOrder(@PathVariable String orderNo) {
        orderService.deleteOrder(orderNo);
        return Result.success("订单已删除", null);
    }

    /**
     * 获取订单总数
     */
    @ApiOperation("获取订单总数")
    @GetMapping("/count")
    public Result<Long> getOrderCount(@RequestParam(required = false) String status) {
        Long count = orderService.getOrderCount(status);
        return Result.success(count);
    }

    /**
     * 从请求中获取管理员ID
     */
    private Long getAdminIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.isAdminToken(token)) {
                return jwtUtil.getAdminIdFromToken(token);
            }
        }
        return null;
    }
}
