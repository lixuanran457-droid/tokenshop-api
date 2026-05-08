-- ============================================
-- TokenShop 系统配置表
-- 用于存储系统运营参数
-- ============================================

USE tokenshop;

-- 系统配置表
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_type` VARCHAR(50) DEFAULT 'string' COMMENT '配置类型: string/number/boolean/json',
    `group_key` VARCHAR(50) DEFAULT 'default' COMMENT '配置分组',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '配置描述',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`),
    KEY `idx_group_key` (`group_key`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 初始化默认配置数据
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_type`, `group_key`, `description`, `sort_order`) VALUES
-- 基础配置
('SITE_NAME', 'COCO CLAW', 'string', 'basic', '网站名称', 1),
('SITE_SLOGAN', '真正便宜的 TOKEN', 'string', 'basic', '网站标语', 2),
('SITE_LOGO', '', 'string', 'basic', '网站Logo URL', 3),
('SITE_FAVICON', '', 'string', 'basic', '网站图标 URL', 4),

-- 用户配置
('NEW_USER_BONUS', '5.00', 'number', 'user', '新用户注册赠送金额(元)', 10),
('FREE_TOKEN_QUOTA', '0', 'number', 'user', '新用户免费Token额度(为0表示不赠送)', 11),
('MIN_RECHARGE_AMOUNT', '1.00', 'number', 'user', '最低充值金额(元)', 12),
('MAX_RECHARGE_AMOUNT', '10000.00', 'number', 'user', '最高单次充值金额(元)', 13),

-- API配置
('API_BASE_URL', 'https://api.openai.com', 'string', 'api', '上游API代理地址', 20),
('API_TIMEOUT', '120', 'number', 'api', 'API请求超时时间(秒)', 21),
('API_MAX_RETRIES', '3', 'number', 'api', 'API最大重试次数', 22),
('DEFAULT_MODEL', 'gpt-3.5-turbo', 'string', 'api', '默认模型ID', 23),

-- 支付配置
('WECHAT_ENABLED', 'true', 'boolean', 'payment', '是否启用微信支付', 30),
('ALIPAY_ENABLED', 'true', 'boolean', 'payment', '是否启用支付宝', 31),
('PAYMENT_NOTIFY_URL', '', 'string', 'payment', '支付回调地址(留空使用系统默认)', 32),

-- 短信/邮件配置
('SMS_ENABLED', 'false', 'boolean', 'notification', '是否启用短信服务', 40),
('EMAIL_ENABLED', 'true', 'boolean', 'notification', '是否启用邮件服务', 41),
('CUSTOMER_SERVICE_EMAIL', 'support@cococlaw.com', 'string', 'notification', '客服邮箱', 42),

-- 系统配置
('MAINTENANCE_MODE', 'false', 'boolean', 'system', '维护模式开关', 50),
('REGISTRATION_ENABLED', 'true', 'boolean', 'system', '是否开放注册', 51),
('LOGIN_VERIFY_CODE', 'true', 'boolean', 'system', '登录是否需要验证码', 52);
