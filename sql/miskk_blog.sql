-- ============================================================
-- 迷失客客の博客 - 全量库 miskk_blog（PostgreSQL 单库，全部表统一存放）
-- ============================================================
-- 在 miskk_blog 库内执行（本机：createdb miskk_blog 后
--   psql -U postgres -d miskk_blog -f sql/miskk_blog.sql
-- docker：postgres 镜像 POSTGRES_DB=miskk_blog 时 initdb 自动执行）。
-- 可重复执行：建表 IF NOT EXISTS；用户/友链/分类/标签幂等冲突跳过，
-- 种子文章与评论按固定 id 段删除后重灌（发布/评论时间按执行日相对计算），
-- 末尾对齐各表 IDENTITY 序列（显式插入 id 不推进序列，不 setval 后续自增会冲突）。

-- ---------------------------- 用户 ----------------------------

CREATE TABLE IF NOT EXISTS t_user (
    id            BIGSERIAL    PRIMARY KEY,
    username      VARCHAR(64)  NOT NULL,
    password      VARCHAR(100) NULL,
    nickname      VARCHAR(32)  NOT NULL,
    avatar        VARCHAR(255) NULL,
    email         VARCHAR(64)  NULL,
    phone         VARCHAR(20)  NULL,
    qq            VARCHAR(20)  NULL,
    wechat        VARCHAR(64)  NULL,
    github        VARCHAR(64)  NULL,
    bio           VARCHAR(255) NULL,
    role          SMALLINT     NOT NULL DEFAULT 0,
    status        SMALLINT     NOT NULL DEFAULT 0,
    last_login_at TIMESTAMP    NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_username UNIQUE (username),
    CONSTRAINT uk_email    UNIQUE (email)
);
CREATE INDEX IF NOT EXISTS idx_github ON t_user (github);

-- 管理员种子账号：admin / admin123（BCrypt 哈希，见 sql/README 说明），头像芙芙（public/chibi Q 版贴纸）
INSERT INTO t_user (username, password, nickname, email, avatar, bio, role, status)
VALUES ('admin', '$2a$10$XPg5sck5VLn2EB0mcKIHt.zTyRZJihfTvLVbM6nt8a/wlzb7.F4GC', '迷失客客', 'admin@miskk.cn', '/chibi/furina.png',
        '一只喜欢写代码的二次元猫咪喵~', 1, 0)
ON CONFLICT (username) DO NOTHING;

-- 演示用户种子：密码统一 admin123（复用管理员 BCrypt 哈希，见 sql/README 说明）。
-- id 从 11 起，避开线上可能已注册的真实用户（自增自然增长区）；
-- t_comment 的种子评论引用这些 id；头像按昵称对应 public/chibi 的 Q 版角色贴纸
INSERT INTO t_user (id, username, password, nickname, email, avatar, bio, role, status, last_login_at, created_at)
VALUES (11, 'xiaoxing', '$2a$10$XPg5sck5VLn2EB0mcKIHt.zTyRZJihfTvLVbM6nt8a/wlzb7.F4GC', '小星尘', 'xiaoxing@miskk.cn', '/chibi/firefly.png',
        '星星光标中毒用户，左键星爆根本停不下来', 0, 0, NOW() - INTERVAL '1 day', NOW() - INTERVAL '62 day'),
       (12, 'paimon', '$2a$10$XPg5sck5VLn2EB0mcKIHt.zTyRZJihfTvLVbM6nt8a/wlzb7.F4GC', '应急食品派蒙', 'paimon@miskk.cn', '/chibi/paimon.png',
        '才不是应急食品！是最好的伙伴！', 0, 0, NOW() - INTERVAL '2 day', NOW() - INTERVAL '55 day'),
       (13, 'stelle', '$2a$10$XPg5sck5VLn2EB0mcKIHt.zTyRZJihfTvLVbM6nt8a/wlzb7.F4GC', '三月七', 'stelle@miskk.cn', '/chibi/march7.png',
        '这次探险一定要载入史册！', 0, 0, NOW() - INTERVAL '3 day', NOW() - INTERVAL '48 day'),
       (14, 'funina', '$2a$10$XPg5sck5VLn2EB0mcKIHt.zTyRZJihfTvLVbM6nt8a/wlzb7.F4GC', '芙宁娜', 'funina@miskk.cn', '/chibi/furina-legacy.png',
        '哼哼，本大人只是路过点评一下', 0, 0, NOW() - INTERVAL '1 day', NOW() - INTERVAL '36 day'),
       (15, 'yinlang', '$2a$10$XPg5sck5VLn2EB0mcKIHt.zTyRZJihfTvLVbM6nt8a/wlzb7.F4GC', '银狼', 'yinlang@miskk.cn', '/chibi/silverwolf.png',
        '随便逛逛，别在意', 0, 0, NOW() - INTERVAL '5 day', NOW() - INTERVAL '21 day')
ON CONFLICT (id) DO NOTHING;

-- 管理员与站长名片使用清晰新图；普通用户和站内 Q 版装饰保留旧版贴纸
UPDATE t_user SET avatar = '/chibi/furina-legacy.png' WHERE role <> 1 AND avatar = '/chibi/furina.png';

-- ---------------------------- 友链 ----------------------------

CREATE TABLE IF NOT EXISTS t_friend_link (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(32)  NOT NULL,
    avatar      VARCHAR(255) NULL,
    url         VARCHAR(255) NOT NULL,
    description VARCHAR(100) NULL,
    status      SMALLINT     NOT NULL DEFAULT 0,
    sort        INTEGER      NOT NULL DEFAULT 99,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_fl_status ON t_friend_link (status);

CREATE TABLE IF NOT EXISTS t_friend_apply (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(32)  NOT NULL,
    avatar      VARCHAR(255) NULL,
    url         VARCHAR(255) NOT NULL,
    description VARCHAR(100) NULL,
    contact     VARCHAR(64)  NOT NULL,
    reason      VARCHAR(200) NULL,
    status      SMALLINT     NOT NULL DEFAULT 0,
    review_note VARCHAR(200) NULL,
    reviewed_at TIMESTAMP    NULL,
    link_id     BIGINT       NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_fa_status ON t_friend_apply (status);

INSERT INTO t_friend_link (id, name, avatar, url, description, status, sort) VALUES
    (1, 'CuteLeaf 的博客', 'https://blog.cuteleaf.cn/favicon.ico', 'https://blog.cuteleaf.cn/', 'Astro 主题 Firefly 作者', 1, 1),
    (2, 'Fuwari', 'https://github.com/saicaca.png', 'https://github.com/saicaca/fuwari', '清新圆润的 Astro 博客模板', 1, 2)
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

-- ---------------------------- 文章域 ----------------------------

CREATE TABLE IF NOT EXISTS t_category (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(32)  NOT NULL,
    description VARCHAR(255) NULL,
    sort        INTEGER      NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_cat_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS t_tag (
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(32)  NOT NULL,
    color      VARCHAR(16)  NULL DEFAULT '#e8639a',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tag_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS t_article (
    id            BIGSERIAL    PRIMARY KEY,
    title         VARCHAR(100) NOT NULL,
    summary       VARCHAR(200) NULL,
    content       TEXT         NOT NULL,
    cover         VARCHAR(255) NULL,
    category_id   BIGINT       NULL,
    author_id     BIGINT       NOT NULL,
    status        SMALLINT     NOT NULL DEFAULT 0,
    pinned        SMALLINT     NOT NULL DEFAULT 0,
    views         BIGINT       NOT NULL DEFAULT 0,
    likes         BIGINT       NOT NULL DEFAULT 0,
    comments      BIGINT       NOT NULL DEFAULT 0,
    shares        BIGINT       NOT NULL DEFAULT 0,
    deleted       SMALLINT     NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at  TIMESTAMP    NULL
);
CREATE INDEX IF NOT EXISTS idx_category        ON t_article (category_id);
CREATE INDEX IF NOT EXISTS idx_author          ON t_article (author_id);
CREATE INDEX IF NOT EXISTS idx_status_published ON t_article (status, published_at);

CREATE TABLE IF NOT EXISTS t_article_tag (
    id         BIGSERIAL PRIMARY KEY,
    article_id BIGINT    NOT NULL,
    tag_id     BIGINT    NOT NULL,
    CONSTRAINT uk_article_tag UNIQUE (article_id, tag_id)
);
CREATE INDEX IF NOT EXISTS idx_at_tag ON t_article_tag (tag_id);

CREATE TABLE IF NOT EXISTS t_comment (
    id            BIGSERIAL     PRIMARY KEY,
    article_id    BIGINT        NOT NULL,
    user_id       BIGINT        NOT NULL,
    parent_id     BIGINT        NULL,
    reply_user_id BIGINT        NULL,
    content       VARCHAR(1000) NOT NULL,
    deleted       SMALLINT      NOT NULL DEFAULT 0,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_c_article ON t_comment (article_id);
CREATE INDEX IF NOT EXISTS idx_c_parent  ON t_comment (parent_id);

CREATE TABLE IF NOT EXISTS t_article_like (
    id         BIGSERIAL PRIMARY KEY,
    article_id BIGINT    NOT NULL,
    user_id    BIGINT    NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_article_user UNIQUE (article_id, user_id)
);

CREATE TABLE IF NOT EXISTS t_share_log (
    id         BIGSERIAL PRIMARY KEY,
    article_id BIGINT    NOT NULL,
    user_id    BIGINT    NULL,
    channel    VARCHAR(16) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_s_article ON t_share_log (article_id);

-- 种子分类
INSERT INTO t_category (id, name, description, sort) VALUES
    (1, '技术分享', '前后端开发技术与踩坑记录', 1),
    (2, '二次元', '动漫、游戏与生活分享', 2),
    (3, '日常随笔', '日常生活碎碎念', 3)
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

-- 种子标签
INSERT INTO t_tag (id, name, color) VALUES
    (1, 'Spring Boot', '#6db33f'),
    (2, 'React', '#61dafb'),
    (3, 'PostgreSQL', '#336791'),
    (4, 'Live2D', '#e8639a'),
    (5, '看板娘', '#9d6bf5'),
    (6, '随笔', '#57b3f0')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

-- 种子文章（作者为管理员 admin，user_id = 1）。
-- 发布时间摊在近 30 天内，便于后台「近 30 天发布趋势」有形状可看；
-- 评论数与下方 t_comment 种子逐篇对应（文 1/2/3/4 = 5/3/3/3）
DELETE FROM t_comment WHERE id BETWEEN 1 AND 14;
DELETE FROM t_article_tag WHERE article_id IN (1, 2, 3, 4);
DELETE FROM t_article WHERE id IN (1, 2, 3, 4);
INSERT INTO t_article (id, title, summary, content, category_id, author_id, status, pinned,
                       views, likes, comments, shares, created_at, updated_at, published_at)
VALUES (1, '迷失客客の博客正式开张啦捏~',
        '欢迎来到迷失客客の博客！本站基于 Spring Boot + Vite React 打造，仙狐看板娘、魔法星星光标和原神崩铁 Q 版小伙伴等你来玩。',
        '# 欢迎来到迷失客客の博客捏~\n\n这是一个用 **Spring Boot 3.3** + **Vite + React 18** 打造的二次元风格博客。\n\n## 站点特色\n\n- 🦊 仙狐小姐 Live2D 看板娘，眼睛会跟着鼠标转\n- ✨ 魔法星星鼠标指针：星星拖尾，左键蹦出字符，右键蹦颜文字\n- ✨ Canvas 柔光尘：固定区域漂浮、鼠标牵引与粒子连线 + 八种主题色随心换\n- 🎈 原神派蒙、崩铁三月七 Q 版小伙伴在站内各处出没\n- 🌗 亮色 / 暗色 / 护眼三种主题\n- 🎵 右上角还有音乐播放器\n\n## 技术栈\n\n| 端 | 技术 |\n|----|------|\n| 前端 | Vite + React + Ant Design |\n| 后端 | Spring Boot 单体应用 |\n| 数据 | PostgreSQL 单库 + Redis 缓存 |\n\n> 觉得不错的话，给文章点个赞再走叭！',
        1, 1, 1, 1, 268, 12, 5, 4,
        NOW() - INTERVAL '27 day', NOW() - INTERVAL '27 day', NOW() - INTERVAL '27 day'),
       (2, 'Spring Boot 单体博客后端设计',
        '单进程吃下全部业务：统一鉴权拦截器、一个 PostgreSQL 管全部数据、Redis 浏览量削峰，聊聊博客后端的分层设计。',
        '# Spring Boot 单体博客后端设计\n\n本站后端是一个单体应用（:8100），请求链路三层：\n\n1. `web.controller` —— 对前端的 REST 接口（`/api/**`，GET/PUT/DELETE 风格）\n2. `web.handler` —— 参数翻译与结果组装，直接调用业务 Service\n3. `service` —— 业务逻辑，全部落到同一个 PostgreSQL 库\n\n## 鉴权是怎么做的？\n\n一个 `AuthInterceptor` 搞定：解析 `Authorization: Bearer` JWT 填充 ThreadLocal 用户上下文，白名单外的请求必须登录，`/api/admin/**` 与标注 `@RequireAdmin` 的接口校验管理员。登录成功后由 Handler 签发 JWT，密钥只有应用自己知道。\n\n## 为什么只用一个 PostgreSQL？\n\n个人博客的数据量，单库单实例绰绰有余：用户、友链、文章、评论全在 `miskk_blog` 一个库里，跨业务写操作共享同一个事务，备份只需 dump 一个库，部署只养一个进程。文章正文用 `TEXT`、标签走关联表，搜索用 `ILIKE` 不区分大小写。\n\n## 为什么浏览量用 Redis？\n\n浏览是高频写操作，直接写库会产生大量行锁竞争。用 Redis `SADD` 按访客按天去重 + Hash 缓冲增量，30 秒批量落库一次：\n\n```java\nredisTemplate.opsForHash().increment(VIEW_BUFFER, articleId, 1);\n```\n\n## 文章作者信息怎么来？\n\n评论和文章列表要展示作者昵称头像，直接查用户表批量取名片，结果用 Redis 缓存 10 分钟。',
        1, 1, 1, 0, 132, 5, 3, 1,
        NOW() - INTERVAL '16 day', NOW() - INTERVAL '16 day', NOW() - INTERVAL '16 day'),
       (3, '用 React 实现一颗跟随鼠标的魔法星星',
        '自定义光标 + SVG 星星：缓慢旋转跟随鼠标，移动洒下星星拖尾，左键星爆蹦出字符，纯前端零依赖实现。',
        '# 用 React 实现一颗跟随鼠标的魔法星星\n\n思路很简单：\n\n1. 隐藏系统光标，画一颗缓慢旋转的四角星作为指针\n2. `mousemove` 里按时间间隔在沿途生成小星星，做成拖尾效果\n3. 监听 `mousedown`：左键 → 星星收缩星爆 + 蹦出字符；右键 → 蹦出颜文字\n\n```tsx\nconst pos = useRef({ x: 0, y: 0 });\nuseEffect(() => {\n  const onMove = (e: MouseEvent) => {\n    pos.current = { x: e.clientX, y: e.clientY };\n  };\n  window.addEventListener(''mousemove'', onMove);\n  return () => window.removeEventListener(''mousemove'', onMove);\n}, []);\n```\n\n记得给容器加 `pointer-events: none`，不然会挡住所有点击捏！',
        1, 1, 1, 0, 98, 4, 3, 1,
        NOW() - INTERVAL '9 day', NOW() - INTERVAL '9 day', NOW() - INTERVAL '9 day'),
       (4, '最近在追的新番与 backlog 管理',
        '十月新番追番清单与我的 spreadsheet 管理法，顺便安利几部治愈系。',
        '# 最近在追的新番与 backlog 管理\n\n十月新番神仙打架！目前的追番表：\n\n- 治愈系日常：每周五准时开饭，下饭神器\n- 长篇热血：已经攒了 3 集，周末一口气补完\n- 偶像企划：吃饭的时候看，人均小剧场\n\n用 Notion 做了个追番看板，状态分为：**在看 / 缓冲区 / 弃了**。\n\n> backlog 越长越有安全感，大概。',
        2, 1, 1, 0, 64, 2, 3, 0,
        NOW() - INTERVAL '3 day', NOW() - INTERVAL '3 day', NOW() - INTERVAL '3 day');

INSERT INTO t_article_tag (article_id, tag_id) VALUES
    (1, 5), (1, 6), (2, 1), (2, 3), (3, 2), (4, 6);

-- 种子评论：用户 id 对应演示用户（11-15）与管理员（1）；
-- 时间摊在近 14 天（每天 1-2 条），便于后台「近 14 天评论数」有数据可看
INSERT INTO t_comment (id, article_id, user_id, parent_id, reply_user_id, content, created_at, updated_at) VALUES
    (1,  1, 11, NULL, NULL, '站开了必须第一个来踩场子！仙狐小姐摸头的时候会发出「咕噜咕噜」的声音吗 (≧▽≦)', (CURRENT_DATE - 13) + TIME '21:30:00', (CURRENT_DATE - 13) + TIME '21:30:00'),
    (2,  1, 12, NULL, NULL, '星星光标太上头了，一路蹦字符，被同事问了好几次用的什么输入法 www', (CURRENT_DATE - 11) + TIME '12:47:00', (CURRENT_DATE - 11) + TIME '12:47:00'),
    (3,  2, 14, NULL, NULL, '一个拦截器管住登录态和权限，鉴权收敛在一处这个设计好优雅，抄走了！', (CURRENT_DATE - 10) + TIME '15:12:00', (CURRENT_DATE - 10) + TIME '15:12:00'),
    (4,  1, 13, NULL, NULL, '护眼主题 + 柔光尘背景，通勤路上刷博客眼睛终于不用受罪了，已加入书签捏~', (CURRENT_DATE - 9) + TIME '08:05:00', (CURRENT_DATE - 9) + TIME '08:05:00'),
    (5,  3, 12, NULL, NULL, '右键蹦颜文字笑死，刚才对着桌面点了一分钟 (´∀`)', (CURRENT_DATE - 8) + TIME '22:41:00', (CURRENT_DATE - 8) + TIME '22:41:00'),
    (6,  2, 15, NULL, NULL, '30 秒批量落库那步，服务重启的时候缓冲里的浏览量会丢吗？', (CURRENT_DATE - 7) + TIME '10:26:00', (CURRENT_DATE - 7) + TIME '10:26:00'),
    (7,  1, 1,  1,    11,   '摸头会触发 TapHead 动作，试试就知道啦，她还会歪头看你哦~', (CURRENT_DATE - 6) + TIME '19:58:00', (CURRENT_DATE - 6) + TIME '19:58:00'),
    (8,  3, 11, NULL, NULL, '拖尾星星的生成间隔是怎么调的？我抄出来的一卡一卡的，完全没有这种丝滑感', (CURRENT_DATE - 5) + TIME '14:33:00', (CURRENT_DATE - 5) + TIME '14:33:00'),
    (9,  4, 15, NULL, NULL, '「弃了」那一栏是不是从来没动过（小声', (CURRENT_DATE - 4) + TIME '23:07:00', (CURRENT_DATE - 4) + TIME '23:07:00'),
    (10, 4, 13, NULL, NULL, '追番看板和在看的数量永远成反比，这就是成年人的平衡吗', (CURRENT_DATE - 3) + TIME '13:20:00', (CURRENT_DATE - 3) + TIME '13:20:00'),
    (11, 2, 12, 3,    14,   '对吧对吧，我也看中这一点，比每个接口自己判一遍登录态省事多了', (CURRENT_DATE - 2) + TIME '17:44:00', (CURRENT_DATE - 2) + TIME '17:44:00'),
    (12, 3, 14, NULL, NULL, 'pointer-events: none 这个坑太真实了，当年被挡点击挡到怀疑人生', (CURRENT_DATE - 1) + TIME '09:15:00', (CURRENT_DATE - 1) + TIME '09:15:00'),
    (13, 1, 14, NULL, NULL, '哼哼，本大人宣布：这个博客的配色勉强配得上本大人的品味（骄傲叉腰）', CURRENT_DATE + TIME '10:02:00', CURRENT_DATE + TIME '10:02:00'),
    (14, 4, 11, 9,    15,   '不会告诉你我的「弃了」栏里躺着 12 部……有生之年系列', CURRENT_DATE + TIME '11:36:00', CURRENT_DATE + TIME '11:36:00');

-- ---------------------------- 序列对齐 ----------------------------
-- 显式插入 id 不推进 IDENTITY 序列；不对齐的话应用下一条 INSERT 会主键冲突

SELECT setval(pg_get_serial_sequence('t_user', 'id'),        GREATEST((SELECT COALESCE(MAX(id), 0) FROM t_user), 1));
SELECT setval(pg_get_serial_sequence('t_friend_link', 'id'), GREATEST((SELECT COALESCE(MAX(id), 0) FROM t_friend_link), 1));
SELECT setval(pg_get_serial_sequence('t_category', 'id'),    GREATEST((SELECT COALESCE(MAX(id), 0) FROM t_category), 1));
SELECT setval(pg_get_serial_sequence('t_tag', 'id'),         GREATEST((SELECT COALESCE(MAX(id), 0) FROM t_tag), 1));
SELECT setval(pg_get_serial_sequence('t_article', 'id'),     GREATEST((SELECT COALESCE(MAX(id), 0) FROM t_article), 1));
SELECT setval(pg_get_serial_sequence('t_comment', 'id'),     GREATEST((SELECT COALESCE(MAX(id), 0) FROM t_comment), 1));
