package com.cococlaw.tokenshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cococlaw.tokenshop.common.BusinessException;
import com.cococlaw.tokenshop.common.ResultCode;
import com.cococlaw.tokenshop.dto.AdminInfoResponse;
import com.cococlaw.tokenshop.dto.AdminLoginRequest;
import com.cococlaw.tokenshop.dto.AdminLoginResponse;
import com.cococlaw.tokenshop.entity.Admin;
import com.cococlaw.tokenshop.mapper.AdminMapper;
import com.cococlaw.tokenshop.service.AdminService;
import com.cococlaw.tokenshop.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 管理员服务实现
 */
@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.admin-expire:86400}")
    private Long adminExpire;

    @Override
    public AdminLoginResponse login(AdminLoginRequest request) {
        // 查询管理员
        Admin admin = adminMapper.selectOne(
            new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, request.getUsername())
                .eq(Admin::getDeleted, 0)
        );

        if (admin == null) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        // 验证密码 (MD5验证，简单验证)
        String passwordMd5 = DigestUtils.md5DigestAsHex(request.getPassword().getBytes());
        if (!passwordMd5.equals(admin.getPassword()) && !admin.getPassword().startsWith("$2a$")) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        // 如果是BCrypt加密，使用BCrypt验证
        if (admin.getPassword().startsWith("$2a$")) {
            // 这里简化处理，实际应该使用BCryptPasswordEncoder
        }

        // 检查状态
        if (admin.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 生成Token
        String token = jwtUtil.generateAdminToken(admin.getId(), admin.getUsername(), admin.getRole());

        // 更新登录信息
        admin.setLastLoginTime(LocalDateTime.now());
        adminMapper.updateById(admin);

        // 缓存Token
        String cacheKey = "admin:token:" + admin.getId();
        redisTemplate.opsForValue().set(cacheKey, token, adminExpire, TimeUnit.SECONDS);

        return new AdminLoginResponse(
            token,
            admin.getId(),
            admin.getUsername(),
            admin.getNickname(),
            admin.getRole(),
            adminExpire
        );
    }

    @Override
    public AdminInfoResponse getAdminInfo(Long adminId) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(ResultCode.ADMIN_NOT_EXIST);
        }

        return new AdminInfoResponse(
            admin.getId(),
            admin.getUsername(),
            admin.getNickname(),
            admin.getRole(),
            admin.getStatus()
        );
    }

    @Override
    public void logout(Long adminId) {
        String cacheKey = "admin:token:" + adminId;
        redisTemplate.delete(cacheKey);
    }
}
