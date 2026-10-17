-- ============================================================
-- 迷失客客の博客 - 用户库
-- ============================================================
-- 声明连接字符集：MySQL 容器内客户端 locale 为 C 时会回退 latin1，
-- 中文种子数据会被按 latin1 双重编码落库成乱码，SET NAMES 对已是
-- utf8mb4 的连接（Navicat/IDEA 等）无副作用
SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS `miskk_user` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `miskk_user`;

CREATE TABLE IF NOT EXISTS `t_user` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`      VARCHAR(64)  NOT NULL COMMENT '登录账号（注册自动生成 / gh_xxx）',
    `password`      VARCHAR(100) NULL COMMENT 'BCrypt 密码哈希，OAuth 用户为空',
    `nickname`      VARCHAR(32)  NOT NULL COMMENT '昵称',
    `avatar`        VARCHAR(255) NULL COMMENT '头像 URL',
    `email`         VARCHAR(64)  NULL COMMENT '邮箱',
    `phone`         VARCHAR(20)  NULL COMMENT '手机号',
    `qq`            VARCHAR(20)  NULL COMMENT 'QQ',
    `wechat`        VARCHAR(64)  NULL COMMENT '微信',
    `github`        VARCHAR(64)  NULL COMMENT 'GitHub 用户名',
    `bio`           VARCHAR(255) NULL COMMENT '个性签名',
    `role`          TINYINT      NOT NULL DEFAULT 0 COMMENT '0 普通用户 1 管理员',
    `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '0 正常 1 封禁',
    `last_login_at` DATETIME     NULL COMMENT '上次登录时间',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_github` (`github`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='用户表';

-- 管理员种子账号：admin / admin123（BCrypt 哈希，见 sql/README 说明）
INSERT INTO `t_user` (`username`, `password`, `nickname`, `email`, `bio`, `role`, `status`)
VALUES ('admin', '$2a$10$XPg5sck5VLn2EB0mcKIHt.zTyRZJihfTvLVbM6nt8a/wlzb7.F4GC', '迷失客客', 'admin@miskk.cn',
        '一只喜欢写代码的二次元猫咪喵~', 1, 0)
ON DUPLICATE KEY UPDATE `username` = `username`;
