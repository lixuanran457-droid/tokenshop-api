package com.cococlaw.tokenshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cococlaw.tokenshop.common.BusinessException;
import com.cococlaw.tokenshop.common.ResultCode;
import com.cococlaw.tokenshop.dto.UserInfoResponse;
import com.cococlaw.tokenshop.entity.User;
import com.cococlaw.tokenshop.mapper.UserMapper;
import com.cococlaw.tokenshop.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 用户服务实现
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

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
}
