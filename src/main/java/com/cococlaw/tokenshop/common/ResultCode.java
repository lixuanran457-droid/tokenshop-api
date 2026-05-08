package com.cococlaw.tokenshop.common;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {
    
    // 成功
    SUCCESS(200, "操作成功"),
    
    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有权限访问该资源"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    
    // 业务错误 6xx
    USER_NOT_EXIST(601, "用户不存在"),
    USER_DISABLED(602, "用户已被禁用"),
    PASSWORD_ERROR(603, "密码错误"),
    PHONE_EXIST(604, "手机号已注册"),
    EMAIL_EXIST(605, "邮箱已注册"),
    PHONE_NOT_EXIST(606, "手机号未注册"),
    EMAIL_NOT_EXIST(607, "邮箱未注册"),
    VERIFY_CODE_ERROR(608, "验证码错误或已过期"),
    VERIFY_CODE_SEND_TOO_FAST(609, "验证码发送太频繁"),
    
    // 业务错误 7xx
    API_KEY_NOT_EXIST(701, "API密钥不存在"),
    API_KEY_DISABLED(702, "API密钥已被禁用"),
    MODEL_NOT_EXIST(703, "模型不存在"),
    MODEL_DISABLED(704, "模型已禁用"),
    PACKAGE_NOT_EXIST(705, "套餐不存在"),
    INSUFFICIENT_BALANCE(706, "余额不足"),
    ORDER_NOT_EXIST(707, "订单不存在"),
    ORDER_PAID(708, "订单已支付"),
    ORDER_CANCELLED(709, "订单已取消"),
    
    // 服务端错误 5xx
    ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
