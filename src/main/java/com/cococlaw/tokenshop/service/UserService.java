package com.cococlaw.tokenshop.service;

import com.cococlaw.tokenshop.dto.UserInfoResponse;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 获取用户信息
     */
    UserInfoResponse getUserInfo(Long userId);

    /**
     * 更新用户余额
     */
    void updateBalance(Long userId, java.math.BigDecimal amount);

    /**
     * 扣减余额
     */
    boolean deductBalance(Long userId, java.math.BigDecimal amount);
}
