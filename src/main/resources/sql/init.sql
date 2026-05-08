-- ============================================
-- TokenShop 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS tokenshop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE tokenshop;

-- ============================================
-- 用户表
-- ============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(加密存储)',
    `balance` DECIMAL(10,2) DEFAULT 0.00 COMMENT '账户余额',
    `total_recharge` DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计充值金额',
    `total_consume` DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计消费金额',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- API密钥表
-- ============================================
DROP TABLE IF EXISTS `api_key`;
CREATE TABLE `api_key` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '密钥ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `api_key` VARCHAR(64) NOT NULL COMMENT 'API密钥',
    `name` VARCHAR(100) DEFAULT '默认密钥' COMMENT '密钥名称',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `last_used_time` DATETIME DEFAULT NULL COMMENT '最后使用时间',
    `total_requests` BIGINT DEFAULT 0 COMMENT '累计请求次数',
    `total_tokens` BIGINT DEFAULT 0 COMMENT '累计消耗Token数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_api_key` (`api_key`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API密钥表';

-- ============================================
-- 模型表
-- ============================================
DROP TABLE IF EXISTS `model`;
CREATE TABLE `model` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模型ID',
    `model_id` VARCHAR(50) NOT NULL COMMENT '模型标识符',
    `name` VARCHAR(100) NOT NULL COMMENT '模型名称',
    `provider` VARCHAR(50) NOT NULL COMMENT '模型厂商',
    `provider_name` VARCHAR(100) NOT NULL COMMENT '厂商全称',
    `description` TEXT COMMENT '模型描述',
    `input_price` DECIMAL(10,4) DEFAULT 0 COMMENT '输入价格(元/1K tokens)',
    `output_price` DECIMAL(10,4) DEFAULT 0 COMMENT '输出价格(元/1K tokens)',
    `context_window` INT DEFAULT 0 COMMENT '上下文窗口大小',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_model_id` (`model_id`),
    KEY `idx_provider` (`provider`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型表';

-- ============================================
-- 套餐表
-- ============================================
DROP TABLE IF EXISTS `package`;
CREATE TABLE `package` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '套餐ID',
    `name` VARCHAR(100) NOT NULL COMMENT '套餐名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '套餐描述',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格(元)',
    `tokens` BIGINT DEFAULT 0 COMMENT '包含Token数量(为0表示按实际用量计费)',
    `validity_days` INT DEFAULT 180 COMMENT '有效期(天)',
    `bonus` DECIMAL(10,2) DEFAULT 0 COMMENT '赠送金额',
    `is_popular` TINYINT DEFAULT 0 COMMENT '是否推荐',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-下架, 1-上架',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_price` (`price`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐表';

-- ============================================
-- 订单表
-- ============================================
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `package_id` BIGINT DEFAULT NULL COMMENT '套餐ID',
    `package_name` VARCHAR(100) DEFAULT NULL COMMENT '套餐名称',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    `actual_amount` DECIMAL(10,2) NOT NULL COMMENT '实际支付金额',
    `pay_type` VARCHAR(20) NOT NULL COMMENT '支付方式: wechat-微信, alipay-支付宝, card-信用卡',
    `pay_status` VARCHAR(20) DEFAULT 'pending' COMMENT '支付状态: pending-待支付, paid-已支付, cancelled-已取消, refunded-已退款',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `trade_no` VARCHAR(128) DEFAULT NULL COMMENT '第三方交易号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_pay_status` (`pay_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ============================================
-- 交易记录表
-- ============================================
DROP TABLE IF EXISTS `transaction`;
CREATE TABLE `transaction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `type` VARCHAR(20) NOT NULL COMMENT '类型: recharge-充值, consume-消费, refund-退款, bonus-赠送',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '金额',
    `balance_before` DECIMAL(10,2) DEFAULT 0 COMMENT '变动前余额',
    `balance_after` DECIMAL(10,2) DEFAULT 0 COMMENT '变动后余额',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `order_no` VARCHAR(64) DEFAULT NULL COMMENT '关联订单号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易记录表';

-- ============================================
-- 使用记录表
-- ============================================
DROP TABLE IF EXISTS `usage_log`;
CREATE TABLE `usage_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `api_key_id` BIGINT NOT NULL COMMENT 'API密钥ID',
    `api_key` VARCHAR(64) NOT NULL COMMENT '使用的API密钥',
    `model_id` VARCHAR(50) NOT NULL COMMENT '调用的模型ID',
    `model_name` VARCHAR(100) DEFAULT NULL COMMENT '模型名称',
    `input_tokens` INT DEFAULT 0 COMMENT '输入Token数',
    `output_tokens` INT DEFAULT 0 COMMENT '输出Token数',
    `total_tokens` INT DEFAULT 0 COMMENT '总Token数',
    `cost` DECIMAL(10,4) DEFAULT 0 COMMENT '消费金额',
    `latency_ms` INT DEFAULT 0 COMMENT '响应延迟(毫秒)',
    `status_code` INT DEFAULT 200 COMMENT '状态码',
    `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT '请求IP',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_api_key_id` (`api_key_id`),
    KEY `idx_model_id` (`model_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='使用记录表';

-- ============================================
-- 初始化数据
-- ============================================

-- 插入模型数据
INSERT INTO `model` (`model_id`, `name`, `provider`, `provider_name`, `description`, `input_price`, `output_price`, `context_window`, `status`, `sort_order`) VALUES
('gpt-4o', 'GPT-4o', 'openai', 'OpenAI', 'OpenAI 最新旗舰模型，支持多模态', 0.0500, 0.1500, 128000, 1, 1),
('gpt-4-turbo', 'GPT-4 Turbo', 'openai', 'OpenAI', '高速版 GPT-4，上下文 128K', 0.1000, 0.3000, 128000, 1, 2),
('gpt-3.5-turbo', 'GPT-3.5 Turbo', 'openai', 'OpenAI', '快速响应的经济型模型', 0.0005, 0.0015, 16385, 1, 3),
('claude-3-5-sonnet', 'Claude 3.5 Sonnet', 'anthropic', 'Anthropic', 'Anthropic 最强模型，编程能力强', 0.0030, 0.0150, 200000, 1, 4),
('claude-3-haiku', 'Claude 3 Haiku', 'anthropic', 'Anthropic', '轻量快速模型', 0.0008, 0.0024, 200000, 1, 5),
('gemini-3.1-pro', 'Gemini 3.1 Pro', 'google', 'Google', 'Google 旗舰模型，超长上下文', 0.0013, 0.0039, 2000000, 1, 6),
('gemini-3.1-flash', 'Gemini 3.1 Flash', 'google', 'Google', '高速响应优化版', 0.0001, 0.0005, 1000000, 1, 7),
('deepseek-v4-pro', 'DeepSeek V4-Pro', 'deepseek', 'DeepSeek', '开源旗舰 MoE 模型', 0.0100, 0.1000, 64000, 1, 8),
('deepseek-v4-flash', 'DeepSeek V4-Flash', 'deepseek', 'DeepSeek', '高速轻量版', 0.0020, 0.0080, 64000, 1, 9),
('kimi-k2.6', 'Kimi K2.6', 'kimi', 'Moonshot AI', '月之暗面最强开源模型', 0.0020, 0.0080, 128000, 1, 10),
('glm-5.1', 'GLM-5.1', 'zhipu', '智谱AI', '智谱最强开源模型', 0.0010, 0.0040, 128000, 1, 11);

-- 插入套餐数据
INSERT INTO `package` (`name`, `description`, `price`, `tokens`, `validity_days`, `bonus`, `is_popular`, `status`, `sort_order`) VALUES
('体验套餐', '新用户试用', 10.00, 0, 180, 0.00, 0, 1, 1),
('基础套餐', '日常使用', 50.00, 0, 365, 5.00, 1, 1, 2),
('进阶套餐', '高频使用', 100.00, 0, 365, 15.00, 0, 1, 3),
('专业套餐', '企业级使用', 500.00, 0, 730, 100.00, 0, 1, 4);

-- 插入测试用户 (密码: 123456)
INSERT INTO `user` (`username`, `phone`, `email`, `password`, `balance`, `status`) VALUES
('测试用户', '13800138000', 'test@cococlaw.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 5.00, 1);
