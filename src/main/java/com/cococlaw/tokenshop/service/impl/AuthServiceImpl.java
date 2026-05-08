package com.cococlaw.tokenshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cococlaw.tokenshop.common.BusinessException;
import com.cococlaw.tokenshop.common.ResultCode;
import com.cococlaw.tokenshop.dto.*;
import com.cococlaw.tokenshop.entity.User;
import com.cococlaw.tokenshop.mapper.UserMapper;
import com.cococlaw.tokenshop.service.AuthService;
import com.cococlaw.tokenshop.utils.JwtUtil;
import com.cococlaw.tokenshop.utils.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
     * 手机号+验证码登录
     */
    @Override
    public AuthResponse phoneLogin(PhoneLoginRequest request) {
        // 验证验证码
        String key = "verify_code:" + request.getPhone() + ":login";
        String cachedCode = (String) redisTemplate.opsForValue().get(key);
        
        if (!request.getCode().equals(cachedCode)) {
            throw new BusinessException(ResultCode.VERIFY_CODE_ERROR);
        }
        
        // 删除已使用的验证码
        redisTemplate.delete(key);

        // 查询用户
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getPhone, request.getPhone())
                .eq(User::getDeleted, 0)
        );

        // 如果用户不存在，自动注册
        if (user == null) {
            user = new User();
            user.setPhone(request.getPhone());
            user.setUsername("用户" + request.getPhone().substring(7));
            user.setBalance(new java.math.BigDecimal("5.00")); // 新用户赠送5元
            user.setStatus(1);
            userMapper.insert(user);
            log.info("自动注册新用户: {}", request.getPhone());
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
     * 邮箱+密码登录
     */
    @Override
    public AuthResponse emailLogin(EmailLoginRequest request) {
        // 查询用户
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail())
                .eq(User::getDeleted, 0)
        );

        if (user == null) {
            throw new BusinessException(ResultCode.EMAIL_NOT_EXIST);
        }

        // 验证密码
        if (!passwordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
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
        user.setBalance(new java.math.BigDecimal("5.00")); // 新用户赠送5元
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
