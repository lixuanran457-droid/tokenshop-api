package com.cococlaw.tokenshop.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 生成Token
     */
    public String generateToken(Long userId, String phone, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        if (phone != null) {
            claims.put("phone", phone);
        }
        if (email != null) {
            claims.put("email", email);
        }
        
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 解析Token
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.warn("Token解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims == null) {
                return false;
            }
            return !isTokenExpired(claims);
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        String userId = claims.getSubject();
        return Long.parseLong(userId);
    }

    /**
     * 获取过期时间(秒)
     */
    public Long getExpiration() {
        return expiration / 1000;
    }

    /**
     * 判断Token是否过期
     */
    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    // ==================== 管理员Token相关 ====================

    /**
     * 生成管理员Token
     */
    public String generateAdminToken(Long adminId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("adminId", adminId);
        claims.put("username", username);
        claims.put("role", role);
        claims.put("type", "admin");

        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(adminId.toString())
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 获取管理员ID
     */
    public Long getAdminIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Object adminId = claims.get("adminId");
        if (adminId == null) {
            return null;
        }
        if (adminId instanceof Integer) {
            return ((Integer) adminId).longValue();
        }
        return (Long) adminId;
    }

    /**
     * 获取管理员角色
     */
    public String getAdminRoleFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return (String) claims.get("role");
    }

    /**
     * 验证是否为管理员Token
     */
    public boolean isAdminToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }
        Object type = claims.get("type");
        return "admin".equals(type);
    }
}
