package com.cococlaw.tokenshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cococlaw.tokenshop.common.BusinessException;
import com.cococlaw.tokenshop.common.ResultCode;
import com.cococlaw.tokenshop.dto.CreateOrderRequest;
import com.cococlaw.tokenshop.dto.OrderResponse;
import com.cococlaw.tokenshop.dto.PackageResponse;
import com.cococlaw.tokenshop.dto.PayResponse;
import com.cococlaw.tokenshop.entity.Order;
import com.cococlaw.tokenshop.entity.Package;
import com.cococlaw.tokenshop.entity.Transaction;
import com.cococlaw.tokenshop.entity.User;
import com.cococlaw.tokenshop.mapper.OrderMapper;
import com.cococlaw.tokenshop.mapper.PackageMapper;
import com.cococlaw.tokenshop.mapper.TransactionMapper;
import com.cococlaw.tokenshop.mapper.UserMapper;
import com.cococlaw.tokenshop.service.OrderService;
import com.cococlaw.tokenshop.service.PackageService;
import com.cococlaw.tokenshop.utils.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务实现
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PackageMapper packageMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private PackageService packageService;

    /**
     * 创建订单
     */
    @Override
    @Transactional
    public PayResponse createOrder(Long userId, CreateOrderRequest request) {
        // 获取套餐信息
        PackageResponse packageInfo = packageService.getPackageById(request.getPackageId());
        Package pkg = packageMapper.selectById(request.getPackageId());

        // 生成订单号
        String orderNo = IdUtil.generateOrderNo();

        // 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setPackageId(request.getPackageId());
        order.setPackageName(packageInfo.getName());
        order.setAmount(packageInfo.getPrice());
        order.setActualAmount(packageInfo.getPrice());
        order.setPayType(request.getPayType());
        order.setPayStatus("pending");

        orderMapper.insert(order);

        log.info("用户{}创建订单: {}, 金额: {}", userId, orderNo, packageInfo.getPrice());

        // 生成支付链接
        return generatePayUrl(orderNo, request.getPayType(), packageInfo.getPrice());
    }

    /**
     * 获取订单列表
     */
    @Override
    public List<OrderResponse> getOrderList(Long userId) {
        List<Order> orders = orderMapper.selectList(
            new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime)
        );

        return orders.stream()
                .map(order -> OrderResponse.builder()
                        .id(order.getId())
                        .orderNo(order.getOrderNo())
                        .packageId(order.getPackageId())
                        .packageName(order.getPackageName())
                        .amount(order.getAmount())
                        .actualAmount(order.getActualAmount())
                        .payType(order.getPayType())
                        .payStatus(order.getPayStatus())
                        .payTime(order.getPayTime())
                        .createTime(order.getCreateTime())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 获取订单详情
     */
    @Override
    public OrderResponse getOrderByNo(String orderNo) {
        Order order = orderMapper.selectOne(
            new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
        );

        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        return OrderResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .packageId(order.getPackageId())
                .packageName(order.getPackageName())
                .amount(order.getAmount())
                .actualAmount(order.getActualAmount())
                .payType(order.getPayType())
                .payStatus(order.getPayStatus())
                .payTime(order.getPayTime())
                .createTime(order.getCreateTime())
                .build();
    }

    /**
     * 支付订单
     */
    @Override
    public PayResponse payOrder(String orderNo, String payType) {
        Order order = orderMapper.selectOne(
            new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
        );

        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        if ("paid".equals(order.getPayStatus())) {
            throw new BusinessException(ResultCode.ORDER_PAID);
        }

        Package pkg = packageMapper.selectById(order.getPackageId());
        return generatePayUrl(orderNo, payType, order.getActualAmount());
    }

    /**
     * 处理支付回调
     */
    @Override
    @Transactional
    public void handlePayCallback(String orderNo, String tradeNo, String payType) {
        Order order = orderMapper.selectOne(
            new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
        );

        if (order == null) {
            log.warn("支付回调订单不存在: {}", orderNo);
            return;
        }

        if ("paid".equals(order.getPayStatus())) {
            log.info("订单已支付，忽略重复回调: {}", orderNo);
            return;
        }

        // 更新订单状态
        orderMapper.update(null,
            new LambdaUpdateWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
                .set(Order::getPayStatus, "paid")
                .set(Order::getPayTime, LocalDateTime.now())
                .set(Order::getTradeNo, tradeNo)
        );

        // 获取套餐信息
        Package pkg = packageMapper.selectById(order.getPackageId());
        BigDecimal totalAmount = order.getActualAmount();
        if (pkg.getBonus() != null && pkg.getBonus().compareTo(BigDecimal.ZERO) > 0) {
            totalAmount = totalAmount.add(pkg.getBonus());
        }

        // 更新用户余额
        User user = userMapper.selectById(order.getUserId());
        BigDecimal balanceBefore = user.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(totalAmount);

        userMapper.update(null,
            new LambdaUpdateWrapper<User>()
                .eq(User::getId, order.getUserId())
                .set(User::getBalance, balanceAfter)
                .set(User::getTotalRecharge, user.getTotalRecharge().add(order.getActualAmount()))
        );

        // 添加充值交易记录
        Transaction transaction = new Transaction();
        transaction.setUserId(order.getUserId());
        transaction.setType("recharge");
        transaction.setAmount(order.getActualAmount());
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription("充值: " + order.getPackageName());
        transaction.setOrderNo(orderNo);
        transactionMapper.insert(transaction);

        log.info("订单{}支付成功，用户{}余额更新: {} -> {}", orderNo, order.getUserId(), balanceBefore, balanceAfter);
    }

    /**
     * 生成支付链接
     */
    private PayResponse generatePayUrl(String orderNo, String payType, BigDecimal amount) {
        // TODO: 实际对接微信支付、支付宝支付接口
        // 这里返回模拟数据，实际生产需要调用第三方支付SDK
        
        if ("wechat".equals(payType)) {
            // 返回微信支付二维码链接
            String qrCode = "weixin://wxpay/bizpayurl?pr=" + orderNo;
            return PayResponse.builder()
                    .orderNo(orderNo)
                    .qrCode(qrCode)
                    .payUrl(qrCode)
                    .build();
        } else if ("alipay".equals(payType)) {
            // 返回支付宝跳转链接
            String payUrl = "https://openapi.alipay.com/gateway.do?out_trade_no=" + orderNo;
            return PayResponse.builder()
                    .orderNo(orderNo)
                    .payUrl(payUrl)
                    .build();
        }
        
        return PayResponse.builder()
                .orderNo(orderNo)
                .build();
    }
}
