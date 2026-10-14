-- ============================================================
-- 迷失客客の博客 - 友链库
-- ============================================================
CREATE DATABASE IF NOT EXISTS `miskk_friend` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `miskk_friend`;

CREATE TABLE IF NOT EXISTS `t_friend_link` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '友链ID',
    `name`        VARCHAR(32)  NOT NULL COMMENT '站点名称',
    `avatar`      VARCHAR(255) NULL COMMENT '站点头像',
    `url`         VARCHAR(255) NOT NULL COMMENT '站点链接',
    `description` VARCHAR(100) NULL COMMENT '站点描述',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '0 待审核 1 已通过 2 已驳回',
    `sort`        INT          NOT NULL DEFAULT 99 COMMENT '排序（越小越靠前）',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='友链';

CREATE TABLE IF NOT EXISTS `t_friend_apply` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '申请ID',
    `name`        VARCHAR(32)  NOT NULL COMMENT '站点名称',
    `avatar`      VARCHAR(255) NULL COMMENT '站点头像',
    `url`         VARCHAR(255) NOT NULL COMMENT '站点链接',
    `description` VARCHAR(100) NULL COMMENT '站点描述',
    `contact`     VARCHAR(64)  NOT NULL COMMENT '联系方式',
    `reason`      VARCHAR(200) NULL COMMENT '申请说明',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '0 待审核 1 通过 2 驳回',
    `review_note` VARCHAR(200) NULL COMMENT '审核备注',
    `reviewed_at` DATETIME     NULL COMMENT '审核时间',
    `link_id`     BIGINT       NULL COMMENT '通过后生成的友链ID',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='友链申请';

-- 种子友链
INSERT INTO `t_friend_link` (`id`, `name`, `avatar`, `url`, `description`, `status`, `sort`) VALUES
    (1, 'CuteLeaf 的博客', 'https://blog.cuteleaf.cn/favicon.ico', 'https://blog.cuteleaf.cn/', 'Astro 主题 Firefly 作者', 1, 1),
    (2, 'Fuwari', 'https://github.com/saicaca.png', 'https://github.com/saicaca/fuwari', '清新圆润的 Astro 博客模板', 1, 2)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);
