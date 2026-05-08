package com.cococlaw.tokenshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cococlaw.tokenshop.common.BusinessException;
import com.cococlaw.tokenshop.common.ResultCode;
import com.cococlaw.tokenshop.dto.*;
import com.cococlaw.tokenshop.entity.User;
import com.cococlaw.tokenshop.mapper.UserMapper;
import com.cococlaw.tokenshop.service.AuthService;
import com.cococlaw.tokenshop.service.SysConfigService;
import com.cococlaw.tokenshop.utils.JwtUtil;
import com.cococlaw.tokenshop.utils.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private SysConfigService sysConfigService;

    /**
     * 发送验证码
     */
    @Override
    public void sendCode(String phoneOrEmail, String type) {
        String key = "verify_code:" + phoneOrEmail + ":" + type;
        
        // 检查是否发送太频繁(60秒内只能发送一次)
        Object lastCode = redisTemplate.opsForValue().get(key + ":last");
        if (lastCode != null) {
            throw new BusinessException(ResultCode.VERIFY_CODE_SEND_TOO_FAST);
        }

        // 生成6位验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        
        // 存储到Redis,5分钟有效
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(key + ":last", "1", 60, TimeUnit.SECONDS);

        // TODO: 实际发送短信/邮件
        // 这里仅打印，实际生产环境需要接入短信网关或邮件服务
        if (phoneOrEmail.contains("@")) {
            log.info("发送邮箱验证码到: {}, 验证码: {}", phoneOrEmail, code);
        } else {
            log.info("发送短信验证码到: {}, 验证码: {}", phoneOrEmail, code);
        }
    }

    /**
     * 统一登录接口
     * 支持密码登录和验证码登录两种方式
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        User user = null;
        
        // 根据账号类型查询用户
        if ("phone".equals(request.getAccountType())) {
            user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getPhone, request.getAccount())
                    .eq(User::getDeleted, 0)
            );
        } else if ("email".equals(request.getAccountType())) {
            user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, request.getAccount())
                    .eq(User::getDeleted, 0)
            );
        }
        
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 密码登录
        if ("password".equals(request.getLoginType())) {
            if (!passwordUtil.matches(request.getPassword(), user.getPassword())) {
                throw new BusinessException(ResultCode.PASSWORD_ERROR);
            }
        }
        // 验证码登录
        else if ("code".equals(request.getLoginType())) {
            // 验证验证码
            String key = "verify_code:" + request.getAccount() + ":login";
            String cachedCode = (String) redisTemplate.opsForValue().get(key);
            
            if (!request.getCode().equals(cachedCode)) {
                throw new BusinessException(ResultCode.VERIFY_CODE_ERROR);
            }
            // 删除已使用的验证码
            redisTemplate.delete(key);
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getEmail());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpiration())
                .userId(user.getId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .email(user.getEmail())
                .balance(user.getBalance())
                .build();
    }

    /**
     * 注册
     * 注册时需要密码
     */
    @Override
    public AuthResponse register(RegisterRequest request) {
        User user = new User();
        user.setUsername(StringUtils.hasText(request.getUsername()) 
            ? request.getUsername() 
            : "用户" + System.currentTimeMillis());

        // 根据注册方式设置手机号或邮箱
        if ("phone".equals(request.getRegisterType())) {
            // 手机号注册需要验证验证码
            String key = "verify_code:" + request.getPhone() + ":register";
            String cachedCode = (String) redisTemplate.opsForValue().get(key);
            
            if (!request.getCode().equals(cachedCode)) {
                throw new BusinessException(ResultCode.VERIFY_CODE_ERROR);
            }
            redisTemplate.delete(key);

            user.setPhone(request.getPhone());
            
            // 检查手机号是否已注册
            User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getPhone, request.getPhone())
                    .eq(User::getDeleted, 0)
            );
            if (existUser != null) {
                throw new BusinessException(ResultCode.PHONE_EXIST);
            }
        } else {
            user.setEmail(request.getEmail());
            
            // 检查邮箱是否已注册
            User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, request.getEmail())
                    .eq(User::getDeleted, 0)
            );
            if (existUser != null) {
                throw new BusinessException(ResultCode.EMAIL_EXIST);
            }
        }

        // 设置密码
        user.setPassword(passwordUtil.encode(request.getPassword()));
        
        // 从配置获取新用户赠送金额
        BigDecimal bonus = sysConfigService.getNumberValue("NEW_USER_BONUS");
        user.setBalance(bonus != null ? bonus : BigDecimal.ZERO);
        
        // 检查是否赠送免费Token额度
        BigDecimal freeTokenQuota = sysConfigService.getNumberValue("FREE_TOKEN_QUOTA", BigDecimal.ZERO);
        if (freeTokenQuota != null && freeTokenQuota.compareTo(BigDecimal.ZERO) > 0) {
            log.info("新注册用户获得免费Token额度: {}", freeTokenQuota);
        }
        
        user.setStatus(1);

        userMapper.insert(user);
        log.info("新用户注册: {}, 类型: {}", 
            "phone".equals(request.getRegisterType()) ? request.getPhone() : request.getEmail(),
            request.getRegisterType());

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getEmail());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpiration())
                .userId(user.getId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .email(user.getEmail())
                .balance(user.getBalance())
                .build();
    }
}
