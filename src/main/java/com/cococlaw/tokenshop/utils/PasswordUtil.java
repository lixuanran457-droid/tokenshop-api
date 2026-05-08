package com.cococlaw.tokenshop.utils;

import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.stereotype.Component;

/**
 * 密码工具类
 */
@Component
public class PasswordUtil {

    /**
     * MD5加密密码
     */
    public String encode(String rawPassword) {
        return DigestUtil.md5Hex(rawPassword);
    }

    /**
     * 验证密码
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return encode(rawPassword).equals(encodedPassword);
    }
}
