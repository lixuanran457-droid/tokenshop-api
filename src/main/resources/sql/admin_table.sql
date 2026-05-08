-- ============================================
-- TokenShop 管理员表 SQL
-- ============================================

USE tokenshop;

-- 管理员表
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role` VARCHAR(20) DEFAULT 'admin' COMMENT '角色: super_admin-超级管理员, admin-普通管理员',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `login_count` INT DEFAULT 0 COMMENT '登录次数',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_status` (`status`),
    KEY `idx_role` (`role`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 管理员操作日志表
DROP TABLE IF EXISTS `admin_log`;
CREATE TABLE `admin_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `admin_id` BIGINT DEFAULT NULL COMMENT '管理员ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '管理员用户名',
    `action` VARCHAR(100) NOT NULL COMMENT '操作类型',
    `target_type` VARCHAR(50) DEFAULT NULL COMMENT '操作对象类型',
    `target_id` VARCHAR(64) DEFAULT NULL COMMENT '操作对象ID',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '操作描述',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '浏览器UA',
    `params` TEXT COMMENT '请求参数(JSON)',
    `result` TINYINT DEFAULT 1 COMMENT '操作结果: 0-失败, 1-成功',
    `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_admin_id` (`admin_id`),
    KEY `idx_action` (`action`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志表';

-- 插入默认管理员
-- 用户名: admin
-- 密码: admin123 (BCrypt加密后的值)
INSERT INTO `admin` (`username`, `password`, `nickname`, `role`, `status`, `remark`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'super_admin', 1, '超级管理员，拥有所有权限');
