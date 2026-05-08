package com.cococlaw.tokenshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cococlaw.tokenshop.common.BusinessException;
import com.cococlaw.tokenshop.common.ResultCode;
import com.cococlaw.tokenshop.dto.UserInfoResponse;
import com.cococlaw.tokenshop.entity.Transaction;
import com.cococlaw.tokenshop.entity.User;
import com.cococlaw.tokenshop.mapper.TransactionMapper;
import com.cococlaw.tokenshop.mapper.UserMapper;
import com.cococlaw.tokenshop.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    /**
     * 获取用户信息
     */
    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        return UserInfoResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .email(user.getEmail())
                .balance(user.getBalance())
                .totalRecharge(user.getTotalRecharge())
                .totalConsume(user.getTotalConsume())
                .createTime(user.getCreateTime())
                .build();
    }

    /**
     * 更新用户余额
     */
    @Override
    @Transactional
    public void updateBalance(Long userId, BigDecimal amount) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        BigDecimal newBalance = user.getBalance().add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ResultCode.INSUFFICIENT_BALANCE);
        }

        userMapper.update(null,
            new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getBalance, newBalance)
        );

        log.info("用户{}余额更新: {} -> {}", userId, user.getBalance(), newBalance);
    }

    /**
     * 扣减余额
     */
    @Override
    @Transactional
    public boolean deductBalance(Long userId, BigDecimal amount) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        if (user.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(ResultCode.INSUFFICIENT_BALANCE);
        }

        BigDecimal newBalance = user.getBalance().subtract(amount);

        userMapper.update(null,
            new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getBalance, newBalance)
                .set(User::getTotalConsume, user.getTotalConsume().add(amount))
        );

        log.info("用户{}扣减余额: {} -> {}, 消费: {}", userId, user.getBalance(), newBalance, amount);
        return true;
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public List<User> getUserList(String keyword, Integer page, Integer pageSize) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(User::getUsername, keyword)
                .or().like(User::getPhone, keyword)
                .or().like(User::getEmail, keyword)
            );
        }

        wrapper.orderByDesc(User::getCreateTime);

        IPage<User> result = userMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return result.getRecords();
    }

    @Override
    public Long getUserCount(String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(User::getUsername, keyword)
                .or().like(User::getPhone, keyword)
                .or().like(User::getEmail, keyword)
            );
        }

        return userMapper.selectCount(wrapper);
    }

    @Override
    @Transactional
    public void disableUser(Long id) {
        userMapper.update(null,
            new LambdaUpdateWrapper<User>()
                .eq(User::getId, id)
                .set(User::getStatus, 0)
        );
        log.info("禁用用户: {}", id);
    }

    @Override
    @Transactional
    public void enableUser(Long id) {
        userMapper.update(null,
            new LambdaUpdateWrapper<User>()
                .eq(User::getId, id)
                .set(User::getStatus, 1)
        );
        log.info("启用用户: {}", id);
    }

    @Override
    @Transactional
    public void adjustBalance(Long id, BigDecimal amount, String reason) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        BigDecimal newBalance = user.getBalance().add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ResultCode.INSUFFICIENT_BALANCE);
        }

        userMapper.update(null,
            new LambdaUpdateWrapper<User>()
                .eq(User::getId, id)
                .set(User::getBalance, newBalance)
        );

        // 记录交易
        Transaction transaction = new Transaction();
        transaction.setUserId(id);
        transaction.setType(amount.compareTo(BigDecimal.ZERO) >= 0 ? "admin_add" : "admin_deduct");
        transaction.setAmount(amount);
        transaction.setBalanceBefore(user.getBalance());
        transaction.setBalanceAfter(newBalance);
        transaction.setDescription(reason != null ? reason : "管理员调整");
        transaction.setCreateTime(LocalDateTime.now());
        transactionMapper.insert(transaction);

        log.info("管理员调整用户{}余额: {} -> {}, 变动: {}, 原因: {}",
            id, user.getBalance(), newBalance, amount, reason);
    }
}
