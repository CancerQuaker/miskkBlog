-- ============================================================
-- 迷失客客の博客 - 文章库
-- ============================================================
-- 声明连接字符集：MySQL 容器内客户端 locale 为 C 时会回退 latin1，
-- 中文种子数据会被按 latin1 双重编码落库成乱码，SET NAMES 对已是
-- utf8mb4 的连接（Navicat/IDEA 等）无副作用
SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS `miskk_article` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `miskk_article`;

CREATE TABLE IF NOT EXISTS `t_category` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name`        VARCHAR(32) NOT NULL COMMENT '分类名',
    `description` VARCHAR(255) NULL COMMENT '描述',
    `sort`        INT         NOT NULL DEFAULT 0 COMMENT '排序',
    `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='文章分类';

CREATE TABLE IF NOT EXISTS `t_tag` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `name`       VARCHAR(32) NOT NULL COMMENT '标签名',
    `color`      VARCHAR(16) NULL DEFAULT '#e8639a' COMMENT '展示颜色',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='文章标签';

CREATE TABLE IF NOT EXISTS `t_article` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '文章ID',
    `title`         VARCHAR(100) NOT NULL COMMENT '标题',
    `summary`       VARCHAR(200) NULL COMMENT '摘要',
    `content`       LONGTEXT     NOT NULL COMMENT 'Markdown 正文',
    `cover`         VARCHAR(255) NULL COMMENT '封面图',
    `category_id`   BIGINT       NULL COMMENT '分类ID',
    `author_id`     BIGINT       NOT NULL COMMENT '作者用户ID（用户服务）',
    `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '0 草稿 1 已发布 2 已隐藏',
    `pinned`        TINYINT      NOT NULL DEFAULT 0 COMMENT '是否置顶',
    `views`         BIGINT       NOT NULL DEFAULT 0 COMMENT '浏览数',
    `likes`         BIGINT       NOT NULL DEFAULT 0 COMMENT '点赞数',
    `comments`      BIGINT       NOT NULL DEFAULT 0 COMMENT '评论数',
    `shares`        BIGINT       NOT NULL DEFAULT 0 COMMENT '分享数',
    `watch_seconds` BIGINT       NOT NULL DEFAULT 0 COMMENT '累计在线观看时长（秒）',
    `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `published_at`  DATETIME     NULL COMMENT '发布时间',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category_id`),
    KEY `idx_author` (`author_id`),
    KEY `idx_status_published` (`status`, `published_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='文章';

CREATE TABLE IF NOT EXISTS `t_article_tag` (
    `id`         BIGINT NOT NULL AUTO_INCREMENT,
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `tag_id`     BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`),
    KEY `idx_tag` (`tag_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='文章标签关联';

CREATE TABLE IF NOT EXISTS `t_comment` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `article_id`    BIGINT       NOT NULL COMMENT '文章ID',
    `user_id`       BIGINT       NOT NULL COMMENT '评论用户ID（用户服务）',
    `parent_id`     BIGINT       NULL COMMENT '父评论ID（顶级为空）',
    `reply_user_id` BIGINT       NULL COMMENT '被回复用户ID',
    `content`       VARCHAR(1000) NOT NULL COMMENT '内容',
    `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_article` (`article_id`),
    KEY `idx_parent` (`parent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='评论';

CREATE TABLE IF NOT EXISTS `t_article_like` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT,
    `article_id` BIGINT   NOT NULL COMMENT '文章ID',
    `user_id`    BIGINT   NOT NULL COMMENT '用户ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_user` (`article_id`, `user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='文章点赞记录';

CREATE TABLE IF NOT EXISTS `t_share_log` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `article_id` BIGINT      NOT NULL COMMENT '文章ID',
    `user_id`    BIGINT      NULL COMMENT '用户ID（匿名分享为空）',
    `channel`    VARCHAR(16) NOT NULL COMMENT '渠道：qq/wechat/copy',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_article` (`article_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='分享日志';

CREATE TABLE IF NOT EXISTS `t_watch_record` (
    `id`          BIGINT  NOT NULL AUTO_INCREMENT,
    `article_id`  BIGINT  NOT NULL COMMENT '文章ID',
    `user_id`     BIGINT  NULL COMMENT '用户ID（匿名访客为空）',
    `visitor_key` VARCHAR(64) NULL COMMENT '匿名访客指纹（IP 哈希）',
    `seconds`     BIGINT  NOT NULL DEFAULT 0 COMMENT '当日累计观看秒数',
    `watch_date`  DATE    NOT NULL COMMENT '观看日期',
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_article_date` (`article_id`, `watch_date`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='观看时长记录';

-- 种子分类
INSERT INTO `t_category` (`id`, `name`, `description`, `sort`) VALUES
    (1, '技术分享', '前后端开发技术与踩坑记录', 1),
    (2, '二次元', '动漫、游戏与生活分享', 2),
    (3, '日常随笔', '日常生活碎碎念', 3)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- 种子标签
INSERT INTO `t_tag` (`id`, `name`, `color`) VALUES
    (1, 'Spring Boot', '#6db33f'),
    (2, 'React', '#61dafb'),
    (3, '微服务', '#f6a522'),
    (4, 'Live2D', '#e8639a'),
    (5, '看板娘', '#9d6bf5'),
    (6, '随笔', '#57b3f0')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- 种子文章（作者为管理员 admin，user_id = 1）
INSERT INTO `t_article` (`id`, `title`, `summary`, `content`, `category_id`, `author_id`, `status`, `pinned`,
                         `views`, `likes`, `comments`, `shares`, `watch_seconds`, `published_at`)
VALUES (1, '迷失客客の博客正式开张啦喵~',
        '欢迎来到迷失客客の博客！本站基于 Spring Boot 微服务 + Vite React 打造，看板娘和猫咪鼠标等你来撸。',
        '# 欢迎来到迷失客客の博客喵~\n\n这是一个用 **Spring Boot 3.3 微服务** + **Vite + React 18** 打造的二次元风格博客。\n\n## 站点特色\n\n- 🐱 猫咪鼠标指针：左键眨眼喵~，右键歪头问号\n- 🌸 樱花飘落特效\n- 👀 Live2D 看板娘，眼睛会跟着鼠标转\n- 🌗 亮色 / 暗色 / 护眼三种主题\n- 🎵 右上角还有音乐播放器\n\n## 技术栈\n\n| 端 | 技术 |\n|----|------|\n| 前端 | Vite + React + Ant Design |\n| 后端 | Spring Boot + Spring Cloud Alibaba |\n| 数据 | MySQL + Redis |\n| 注册中心 | Nacos |\n\n> 觉得不错的话，给文章点个赞再走叭！',
        1, 1, 1, 1, 128, 5, 1, 2, 3600, NOW()),
       (2, 'Spring Boot 微服务博客后端设计',
        '从单体到微服务：网关鉴权、Nacos 注册中心、Feign 内部调用、Redis 浏览量缓冲，聊聊博客后端的分层设计。',
        '# Spring Boot 微服务博客后端设计\n\n本站后端由 5 个服务组成：\n\n1. `miskk-gateway` 统一入口，JWT 鉴权\n2. `miskk-service-user` 用户与认证\n3. `miskk-service-article` 文章域\n4. `miskk-service-friend` 友链域\n5. `miskk-service-admin` 统计聚合与系统监控\n\n## 为什么浏览量用 Redis？\n\n浏览是高频写操作，直接写库会产生大量行锁竞争。我们用 Redis Hash 做增量缓冲，定时任务每 30 秒批量落库。\n\n```java\nredisTemplate.opsForHash().increment(VIEW_BUFFER, articleId, 1);\n```\n\n## 服务间如何取用户昵称？\n\n评论和文章列表需要展示作者信息，通过 OpenFeign 调用用户服务的内部接口，结果用 Redis 缓存 10 分钟。',
        1, 1, 1, 0, 96, 3, 0, 1, 1800, NOW()),
       (3, '用 React 实现一只趴在鼠标上的猫',
        '自定义光标 + SVG 猫咪：跟随鼠标、左键眨眼发喵~、右键歪头冒问号，纯前端零依赖实现。',
        '# 用 React 实现一只趴在鼠标上的猫\n\n思路很简单：\n\n1. 隐藏系统光标，画一个圆点作为准星\n2. 用 `requestAnimationFrame` 做插值，让猫有一点点"跟不上"的Q弹感\n3. 监听 `mousedown`：左键 → 闭眼 + "喵~" 气泡；右键 → 歪头 + "？" 气泡\n\n```tsx\nconst pos = useRef({ x: 0, y: 0 });\nuseEffect(() => {\n  const onMove = (e: MouseEvent) => {\n    pos.current = { x: e.clientX, y: e.clientY };\n  };\n  window.addEventListener(\'\'mousemove\'\', onMove);\n  return () => window.removeEventListener(\'\'mousemove\'\', onMove);\n}, []);\n```\n\n记得给容器加 `pointer-events: none`，不然会挡住所有点击喵！',
        1, 1, 1, 0, 76, 2, 0, 1, 900, NOW()),
       (4, '最近在追的新番与 backlog 管理',
        '七月新番追番清单与我的 spreadsheet 管理法，顺便安利几部治愈系。',
        '# 最近在追的新番与 backlog 管理\n\n七月新番神仙打架！目前的追番表：\n\n- 治愈系A：每周五更新\n- 热血B：已经攒了 3 集\n- 日常C：吃饭的时候看\n\n用 Notion 做了个追番看板，状态分为：**在看 / 缓冲区 / 弃了**。',
        2, 1, 1, 0, 51, 1, 0, 0, 240, NOW())
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`);

INSERT INTO `t_article_tag` (`article_id`, `tag_id`) VALUES
    (1, 5), (1, 6), (2, 1), (2, 3), (3, 2), (3, 4), (4, 6)
ON DUPLICATE KEY UPDATE `article_id` = VALUES(`article_id`);
