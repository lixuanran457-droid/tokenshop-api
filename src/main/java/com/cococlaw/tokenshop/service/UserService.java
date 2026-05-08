package com.cococlaw.tokenshop.service;

import com.cococlaw.tokenshop.dto.UserInfoResponse;
import com.cococlaw.tokenshop.entity.User;

import java.math.BigDecimal;
import java.util.List;

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
    void updateBalance(Long userId, BigDecimal amount);

    /**
     * 扣减余额
     */
    boolean deductBalance(Long userId, BigDecimal amount);

    /**
     * 根据ID获取用户
     */
    User getById(Long id);

    /**
     * 获取用户列表（管理员）
     */
    List<User> getUserList(String keyword, Integer page, Integer pageSize);

    /**
     * 获取用户总数（管理员）
     */
    Long getUserCount(String keyword);

    /**
     * 禁用用户
     */
    void disableUser(Long id);

    /**
     * 启用用户
     */
    void enableUser(Long id);

    /**
     * 调整用户余额（管理员）
     */
    void adjustBalance(Long id, BigDecimal amount, String reason);
}
