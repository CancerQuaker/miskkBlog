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

-- 种子文章（作者为管理员 admin，user_id = 1）。
-- 发布时间摊在近 30 天内，便于后台「近 30 天发布趋势」有形状可看；
-- 评论数与下方 t_comment 种子逐篇对应
INSERT INTO `t_article` (`id`, `title`, `summary`, `content`, `category_id`, `author_id`, `status`, `pinned`,
                         `views`, `likes`, `comments`, `shares`, `watch_seconds`, `published_at`)
VALUES (1, '迷失客客の博客正式开张啦捏~',
        '欢迎来到迷失客客の博客！本站基于 Spring Boot 微服务 + Vite React 打造，仙狐看板娘、魔法星星光标和原神崩铁 Q 版小伙伴等你来玩。',
        '# 欢迎来到迷失客客の博客捏~\n\n这是一个用 **Spring Boot 3.3 微服务** + **Vite + React 18** 打造的二次元风格博客。\n\n## 站点特色\n\n- 🦊 仙狐小姐 Live2D 看板娘，眼睛会跟着鼠标转\n- ✨ 魔法星星鼠标指针：星星拖尾，左键蹦出字符，右键蹦颜文字\n- 🌸 樱花飘落特效 + 八种主题色随心换\n- 🎈 原神派蒙、崩铁三月七 Q 版小伙伴在站内各处出没\n- 🌗 亮色 / 暗色 / 护眼三种主题\n- 🎵 右上角还有音乐播放器\n\n## 技术栈\n\n| 端 | 技术 |\n|----|------|\n| 前端 | Vite + React + Ant Design |\n| 后端 | Spring Boot + Spring Cloud Alibaba |\n| 数据 | MySQL + Redis |\n| 注册中心 | Nacos |\n\n> 觉得不错的话，给文章点个赞再走叭！',
        1, 1, 1, 1, 268, 12, 5, 4, 4200, NOW() - INTERVAL 27 DAY),
       (2, 'Spring Boot 微服务博客后端设计',
        '从单体到微服务：网关签发 JWT、X-User-Info 单头下发身份、内部接口令牌、Redis 浏览量削峰，聊聊博客后端的分层设计。',
        '# Spring Boot 微服务博客后端设计\n\n本站后端由 3 个服务组成：\n\n1. `miskk-gateway`（:8100）—— 唯一对外入口，JWT 的签发与解析都在网关\n2. `miskk-service-user`（:8101）—— 用户与认证\n3. `miskk-service-blog`（:8102）—— 文章 / 评论 / 友链 / 管理端聚合；文章库为主数据源，友链库走 `@DS("friend")` 动态路由\n\n## 鉴权是怎么下发的？\n\n`JWT_SECRET` 只有网关配置。校验通过后，网关把登录身份序列化成 JSON 塞进 `X-User-Info` 请求头经 Feign 下发，下游拦截器还原 ThreadLocal 并校验 `@RequireLogin / @RequireAdmin`。\n\n## 内部接口怎么防裸奔？\n\n`/internal/**` 只接受带 `X-Internal-Token` 的调用；服务间 Feign 直连不走网关，公共头统一封装在 miskk-common。\n\n## 为什么浏览量用 Redis？\n\n浏览是高频写操作，直接写库会产生大量行锁竞争。用 Redis `SADD` 按访客按天去重 + Hash 缓冲增量，30 秒批量落库一次：\n\n```java\nredisTemplate.opsForHash().increment(VIEW_BUFFER, articleId, 1);\n```\n\n## 服务间如何取用户昵称？\n\n评论和文章列表需要展示作者信息，通过 OpenFeign 调用用户服务的内部接口，结果用 Redis 缓存 10 分钟。',
        1, 1, 1, 0, 132, 5, 3, 1, 1900, NOW() - INTERVAL 16 DAY),
       (3, '用 React 实现一颗跟随鼠标的魔法星星',
        '自定义光标 + SVG 星星：缓慢旋转跟随鼠标，移动洒下星星拖尾，左键星爆蹦出字符，纯前端零依赖实现。',
        '# 用 React 实现一颗跟随鼠标的魔法星星\n\n思路很简单：\n\n1. 隐藏系统光标，画一颗缓慢旋转的四角星作为指针\n2. `mousemove` 里按时间间隔在沿途生成小星星，做成拖尾效果\n3. 监听 `mousedown`：左键 → 星星收缩星爆 + 蹦出字符；右键 → 蹦出颜文字\n\n```tsx\nconst pos = useRef({ x: 0, y: 0 });\nuseEffect(() => {\n  const onMove = (e: MouseEvent) => {\n    pos.current = { x: e.clientX, y: e.clientY };\n  };\n  window.addEventListener(\'\'mousemove\'\', onMove);\n  return () => window.removeEventListener(\'\'mousemove\'\', onMove);\n}, []);\n```\n\n记得给容器加 `pointer-events: none`，不然会挡住所有点击捏！',
        1, 1, 1, 0, 98, 4, 3, 1, 950, NOW() - INTERVAL 9 DAY),
       (4, '最近在追的新番与 backlog 管理',
        '十月新番追番清单与我的 spreadsheet 管理法，顺便安利几部治愈系。',
        '# 最近在追的新番与 backlog 管理\n\n十月新番神仙打架！目前的追番表：\n\n- 治愈系日常：每周五准时开饭，下饭神器\n- 长篇热血：已经攒了 3 集，周末一口气补完\n- 偶像企划：吃饭的时候看，人均小剧场\n\n用 Notion 做了个追番看板，状态分为：**在看 / 缓冲区 / 弃了**。\n\n> backlog 越长越有安全感，大概。',
        2, 1, 1, 0, 64, 2, 3, 0, 280, NOW() - INTERVAL 3 DAY)
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`), `summary` = VALUES(`summary`), `content` = VALUES(`content`),
    `category_id` = VALUES(`category_id`), `status` = VALUES(`status`), `pinned` = VALUES(`pinned`),
    `views` = VALUES(`views`), `likes` = VALUES(`likes`), `comments` = VALUES(`comments`),
    `shares` = VALUES(`shares`), `watch_seconds` = VALUES(`watch_seconds`), `published_at` = VALUES(`published_at`);

-- 先清掉种子文章的旧标签再写入（重复导入保持确定性，且不影响种子之外的文章）
DELETE FROM `t_article_tag` WHERE `article_id` IN (1, 2, 3, 4);
INSERT INTO `t_article_tag` (`article_id`, `tag_id`) VALUES
    (1, 5), (1, 6), (2, 1), (2, 3), (3, 2), (4, 6);

-- 种子评论：用户 id 对应 01_miskk_user.sql 的演示用户（11-15）与管理员（1）；
-- 时间摊在近 14 天（每天 1-2 条），便于后台「近 14 天评论数」有数据可看，
-- 各文章条数与上方 t_article.comments 种子一致（文 1/2/3/4 = 5/3/3/3）
DELETE FROM `t_comment` WHERE `id` BETWEEN 1 AND 14;
INSERT INTO `t_comment` (`id`, `article_id`, `user_id`, `parent_id`, `reply_user_id`, `content`, `created_at`)
VALUES (1, 1, 11, NULL, NULL, '站开了必须第一个来踩场子！仙狐小姐摸头的时候会发出「咕噜咕噜」的声音吗 (≧▽≦)', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 13 DAY), '21:30:00')),
       (2, 1, 12, NULL, NULL, '星星光标太上头了，一路蹦字符，被同事问了好几次用的什么输入法 www', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 11 DAY), '12:47:00')),
       (3, 2, 14, NULL, NULL, 'X-User-Info 单头下发这个设计好优雅，比拖一串请求头干净多了，抄走了！', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 10 DAY), '15:12:00')),
       (4, 1, 13, NULL, NULL, '护眼主题 + 樱花特效，通勤路上刷博客眼睛终于不用受罪了，已加入书签捏~', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 9 DAY), '08:05:00')),
       (5, 3, 12, NULL, NULL, '右键蹦颜文字笑死，刚才对着桌面点了一分钟 (´∀`)', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 8 DAY), '22:41:00')),
       (6, 2, 15, NULL, NULL, '30 秒批量落库那步，服务重启的时候缓冲里的浏览量会丢吗？', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 7 DAY), '10:26:00')),
       (7, 1, 1, 1, 11, '摸头会触发 TapHead 动作，试试就知道啦，她还会歪头看你哦~', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '19:58:00')),
       (8, 3, 11, NULL, NULL, '拖尾星星的生成间隔是怎么调的？我抄出来的一卡一卡的，完全没有这种丝滑感', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '14:33:00')),
       (9, 4, 15, NULL, NULL, '「弃了」那一栏是不是从来没动过（小声', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '23:07:00')),
       (10, 4, 13, NULL, NULL, '追番看板和在看的数量永远成反比，这就是成年人的平衡吗', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '13:20:00')),
       (11, 2, 12, 3, 14, '对吧对吧，我也看中这一点，比每个服务都解析一遍 JWT 省事多了', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '17:44:00')),
       (12, 3, 14, NULL, NULL, 'pointer-events: none 这个坑太真实了，当年被挡点击挡到怀疑人生', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:15:00')),
       (13, 1, 14, NULL, NULL, '哼哼，本大人宣布：这个博客的配色勉强配得上本大人的品味（骄傲叉腰）', TIMESTAMP(CURDATE(), '10:02:00')),
       (14, 4, 11, 9, 15, '不会告诉你我的「弃了」栏里躺着 12 部……有生之年系列', TIMESTAMP(CURDATE(), '11:36:00'));
