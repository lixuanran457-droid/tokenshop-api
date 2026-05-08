package com.cococlaw.tokenshop.utils;

import cn.hutool.core.lang.UUID;
import org.springframework.stereotype.Component;

/**
 * ID生成工具类
 */
@Component
public class IdUtil {

    /**
     * 生成UUID (无横线)
     */
    public static String uuid() {
        return UUID.fastUUID().toString(true);
    }

    /**
     * 生成带横线的UUID
     */
    public static String uuidWithDash() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成API密钥
     * 格式: sk-coco-{32位随机字符}
     */
    public static String generateApiKey() {
        return "sk-coco-" + uuid().toUpperCase();
    }

    /**
     * 生成订单号
     * 格式: {时间戳}{随机数}
     */
    public static String generateOrderNo() {
        return System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
    }
}
