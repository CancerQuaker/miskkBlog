// ============================================================
// 迷失客客の博客 - 文章库 miskk_article（MongoDB：文章 / 分类 / 标签 / 评论 / 互动）
// ============================================================
// 基础数据（用户 / 友链）在 MySQL（见 sql/mysql/miskk_base.sql）。
//
// 用 mongosh 执行：mongosh "mongodb://127.0.0.1:27017" --file miskk_article.js
// docker 部署时由 mongo 镜像 /docker-entrypoint-initdb.d 自动执行（首次空数据卷）。
// 字段名与后端 document 类的 Java 属性一一对应（_id 即实体 Long id）。
// 重复执行安全：先删除固定 id 段的种子文档再写入，不影响种子之外的数据。

const db = db.getSiblingDB('miskk_article');

// ---------------------------- 索引 ----------------------------
// 索引不在本脚本创建：后端 document 类上的 @Indexed / @CompoundIndex 注解
// 会在应用首次启动时统一建索引（spring.data.mongodb.auto-index-creation=true），
// 避免与注解定义的索引名冲突（Mongo 同键不同名会报 IndexOptionsConflict）。
// 关键索引：article(status+publishedAt / categoryId / tags)、comment(articleId+createdAt / parentId)、
// article_like(articleId+userId 唯一)、watch_record(articleId+watchDate+userId / +visitorKey)、tag(name 唯一)。

// ---------------------------- 工具 ----------------------------

const now = () => new Date();
// 相对当前日期偏移 n 天的某时刻（与原 MySQL 种子的 DATE_SUB(CURDATE(), INTERVAL n DAY) 对齐）
const dateAt = (offsetDays, hm) => {
  const d = new Date();
  d.setHours(0, 0, 0, 0);
  d.setDate(d.getDate() - offsetDays);
  const [h, m, s] = hm.split(':').map(Number);
  d.setHours(h, m, s);
  return d;
};
const daysAgo = (n) => dateAt(n, '10:00:00');

// ---------------------------- 种子：分类 / 标签 ----------------------------

db.category.deleteMany({ _id: { $in: [1, 2, 3] } });
db.category.insertMany([
  { _id: 1, name: '技术分享', description: '前后端开发技术与踩坑记录', sort: 1, createdAt: daysAgo(70), updatedAt: daysAgo(70) },
  { _id: 2, name: '二次元', description: '动漫、游戏与生活分享', sort: 2, createdAt: daysAgo(70), updatedAt: daysAgo(70) },
  { _id: 3, name: '日常随笔', description: '日常生活碎碎念', sort: 3, createdAt: daysAgo(70), updatedAt: daysAgo(70) },
]);

db.tag.deleteMany({ _id: { $in: [1, 2, 3, 4, 5, 6] } });
db.tag.insertMany([
  { _id: 1, name: 'Spring Boot', color: '#6db33f', createdAt: daysAgo(70), updatedAt: daysAgo(70) },
  { _id: 2, name: 'React', color: '#61dafb', createdAt: daysAgo(70), updatedAt: daysAgo(70) },
  { _id: 3, name: 'MongoDB', color: '#4faa41', createdAt: daysAgo(70), updatedAt: daysAgo(70) },
  { _id: 4, name: 'Live2D', color: '#e8639a', createdAt: daysAgo(70), updatedAt: daysAgo(70) },
  { _id: 5, name: '看板娘', color: '#9d6bf5', createdAt: daysAgo(70), updatedAt: daysAgo(70) },
  { _id: 6, name: '随笔', color: '#57b3f0', createdAt: daysAgo(70), updatedAt: daysAgo(70) },
]);

// ---------------------------- 种子：文章 ----------------------------
// 作者为管理员 admin（user_id = 1）；发布时间摊在近 30 天内，便于后台趋势图有形状；
// 评论数与下方 comment 种子逐篇对应（文 1/2/3/4 = 5/3/3/3）

db.article.deleteMany({ _id: { $in: [1, 2, 3, 4] } });
db.article.insertMany([
  {
    _id: 1, title: '迷失客客の博客正式开张啦捏~',
    summary: '欢迎来到迷失客客の博客！本站基于 Spring Boot + Vite React 打造，仙狐看板娘、魔法星星光标和原神崩铁 Q 版小伙伴等你来玩。',
    content: '# 欢迎来到迷失客客の博客捏~\n\n这是一个用 **Spring Boot 3.3** + **Vite + React 18** 打造的二次元风格博客。\n\n## 站点特色\n\n- 🦊 仙狐小姐 Live2D 看板娘，眼睛会跟着鼠标转\n- ✨ 魔法星星鼠标指针：星星拖尾，左键蹦出字符，右键蹦颜文字\n- 🌸 樱花飘落特效 + 八种主题色随心换\n- 🎈 原神派蒙、崩铁三月七 Q 版小伙伴在站内各处出没\n- 🌗 亮色 / 暗色 / 护眼三种主题\n- 🎵 右上角还有音乐播放器\n\n## 技术栈\n\n| 端 | 技术 |\n|----|------|\n| 前端 | Vite + React + Ant Design |\n| 后端 | Spring Boot 单体应用 |\n| 数据 | MySQL（基础数据）+ MongoDB（文章）|\n| 缓存 | Redis |\n\n> 觉得不错的话，给文章点个赞再走叭！',
    cover: null, categoryId: 1, authorId: 1, status: 1, pinned: 1,
    views: 268, likes: 12, comments: 5, shares: 4, watchSeconds: 4200,
    tags: [5, 6], deleted: false,
    createdAt: daysAgo(27), updatedAt: daysAgo(27), publishedAt: daysAgo(27),
  },
  {
    _id: 2, title: 'Spring Boot 单体博客后端设计',
    summary: '单进程吃下全部业务：统一鉴权拦截器、MySQL 管基础数据、MongoDB 存文章全家族、Redis 浏览量削峰，聊聊博客后端的分层设计。',
    content: '# Spring Boot 单体博客后端设计\n\n本站后端是一个单体应用（:8100），请求链路三层：\n\n1. `web.controller` —— 对前端的 REST 接口（`/api/**`，GET/PUT/DELETE 风格）\n2. `web.handler` —— 参数翻译与结果组装，直接调用业务 Service\n3. `service` —— 业务逻辑，按数据归属分别落到 MySQL 与 MongoDB\n\n## 鉴权是怎么做的？\n\n一个 `AuthInterceptor` 搞定：解析 `Authorization: Bearer` JWT 填充 ThreadLocal 用户上下文，白名单外的请求必须登录，`/api/admin/**` 与标注 `@RequireAdmin` 的接口校验管理员。登录成功后由 Handler 签发 JWT，密钥只有应用自己知道。\n\n## 为什么文章放 MongoDB？\n\n用户、友链这类关系型基础数据留在 MySQL；文章、分类、标签、评论、点赞、观看记录这些"内容家族"整体放进 MongoDB：标签从关联表变成文章文档里的数组，详情页一次查询就能带出全部内容，正文这种大字段也不怕行越来越宽。\n\n## 为什么浏览量用 Redis？\n\n浏览是高频写操作，直接写库会产生大量行锁竞争。用 Redis `SADD` 按访客按天去重 + Hash 缓冲增量，30 秒批量落库一次：\n\n```java\nredisTemplate.opsForHash().increment(VIEW_BUFFER, articleId, 1);\n```\n\n## 文章作者信息怎么来？\n\n评论和文章列表要展示作者昵称头像，直接查 MySQL 用户表批量取名片，结果用 Redis 缓存 10 分钟。',
    cover: null, categoryId: 1, authorId: 1, status: 1, pinned: 0,
    views: 132, likes: 5, comments: 3, shares: 1, watchSeconds: 1900,
    tags: [1, 3], deleted: false,
    createdAt: daysAgo(16), updatedAt: daysAgo(16), publishedAt: daysAgo(16),
  },
  {
    _id: 3, title: '用 React 实现一颗跟随鼠标的魔法星星',
    summary: '自定义光标 + SVG 星星：缓慢旋转跟随鼠标，移动洒下星星拖尾，左键星爆蹦出字符，纯前端零依赖实现。',
    content: '# 用 React 实现一颗跟随鼠标的魔法星星\n\n思路很简单：\n\n1. 隐藏系统光标，画一颗缓慢旋转的四角星作为指针\n2. `mousemove` 里按时间间隔在沿途生成小星星，做成拖尾效果\n3. 监听 `mousedown`：左键 → 星星收缩星爆 + 蹦出字符；右键 → 蹦出颜文字\n\n```tsx\nconst pos = useRef({ x: 0, y: 0 });\nuseEffect(() => {\n  const onMove = (e: MouseEvent) => {\n    pos.current = { x: e.clientX, y: e.clientY };\n  };\n  window.addEventListener(\'\'mousemove\'\', onMove);\n  return () => window.removeEventListener(\'\'mousemove\'\', onMove);\n}, []);\n```\n\n记得给容器加 `pointer-events: none`，不然会挡住所有点击捏！',
    cover: null, categoryId: 1, authorId: 1, status: 1, pinned: 0,
    views: 98, likes: 4, comments: 3, shares: 1, watchSeconds: 950,
    tags: [2], deleted: false,
    createdAt: daysAgo(9), updatedAt: daysAgo(9), publishedAt: daysAgo(9),
  },
  {
    _id: 4, title: '最近在追的新番与 backlog 管理',
    summary: '十月新番追番清单与我的 spreadsheet 管理法，顺便安利几部治愈系。',
    content: '# 最近在追的新番与 backlog 管理\n\n十月新番神仙打架！目前的追番表：\n\n- 治愈系日常：每周五准时开饭，下饭神器\n- 长篇热血：已经攒了 3 集，周末一口气补完\n- 偶像企划：吃饭的时候看，人均小剧场\n\n用 Notion 做了个追番看板，状态分为：**在看 / 缓冲区 / 弃了**。\n\n> backlog 越长越有安全感，大概。',
    cover: null, categoryId: 2, authorId: 1, status: 1, pinned: 0,
    views: 64, likes: 2, comments: 3, shares: 0, watchSeconds: 280,
    tags: [6], deleted: false,
    createdAt: daysAgo(3), updatedAt: daysAgo(3), publishedAt: daysAgo(3),
  },
]);

// ---------------------------- 种子：评论 ----------------------------
// 用户 id 对应 MySQL 种子用户（11-15 演示用户与 1 管理员，见 sql/mysql/miskk_base.sql）；
// 时间摊在近 14 天（每天 1-2 条），便于后台「近 14 天评论数」有数据可看

db.comment.deleteMany({ _id: { $in: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14] } });
db.comment.insertMany([
  { _id: 1, articleId: 1, userId: 11, parentId: null, replyUserId: null, content: '站开了必须第一个来踩场子！仙狐小姐摸头的时候会发出「咕噜咕噜」的声音吗 (≧▽≦)', deleted: false, createdAt: dateAt(13, '21:30:00'), updatedAt: dateAt(13, '21:30:00') },
  { _id: 2, articleId: 1, userId: 12, parentId: null, replyUserId: null, content: '星星光标太上头了，一路蹦字符，被同事问了好几次用的什么输入法 www', deleted: false, createdAt: dateAt(11, '12:47:00'), updatedAt: dateAt(11, '12:47:00') },
  { _id: 3, articleId: 2, userId: 14, parentId: null, replyUserId: null, content: '一个拦截器管住登录态和权限，鉴权收敛在一处这个设计好优雅，抄走了！', deleted: false, createdAt: dateAt(10, '15:12:00'), updatedAt: dateAt(10, '15:12:00') },
  { _id: 4, articleId: 1, userId: 13, parentId: null, replyUserId: null, content: '护眼主题 + 樱花特效，通勤路上刷博客眼睛终于不用受罪了，已加入书签捏~', deleted: false, createdAt: dateAt(9, '08:05:00'), updatedAt: dateAt(9, '08:05:00') },
  { _id: 5, articleId: 3, userId: 12, parentId: null, replyUserId: null, content: '右键蹦颜文字笑死，刚才对着桌面点了一分钟 (´∀`)', deleted: false, createdAt: dateAt(8, '22:41:00'), updatedAt: dateAt(8, '22:41:00') },
  { _id: 6, articleId: 2, userId: 15, parentId: null, replyUserId: null, content: '30 秒批量落库那步，服务重启的时候缓冲里的浏览量会丢吗？', deleted: false, createdAt: dateAt(7, '10:26:00'), updatedAt: dateAt(7, '10:26:00') },
  { _id: 7, articleId: 1, userId: 1, parentId: 1, replyUserId: 11, content: '摸头会触发 TapHead 动作，试试就知道啦，她还会歪头看你哦~', deleted: false, createdAt: dateAt(6, '19:58:00'), updatedAt: dateAt(6, '19:58:00') },
  { _id: 8, articleId: 3, userId: 11, parentId: null, replyUserId: null, content: '拖尾星星的生成间隔是怎么调的？我抄出来的一卡一卡的，完全没有这种丝滑感', deleted: false, createdAt: dateAt(5, '14:33:00'), updatedAt: dateAt(5, '14:33:00') },
  { _id: 9, articleId: 4, userId: 15, parentId: null, replyUserId: null, content: '「弃了」那一栏是不是从来没动过（小声', deleted: false, createdAt: dateAt(4, '23:07:00'), updatedAt: dateAt(4, '23:07:00') },
  { _id: 10, articleId: 4, userId: 13, parentId: null, replyUserId: null, content: '追番看板和在看的数量永远成反比，这就是成年人的平衡吗', deleted: false, createdAt: dateAt(3, '13:20:00'), updatedAt: dateAt(3, '13:20:00') },
  { _id: 11, articleId: 2, userId: 12, parentId: 3, replyUserId: 14, content: '对吧对吧，我也看中这一点，比每个接口自己判一遍登录态省事多了', deleted: false, createdAt: dateAt(2, '17:44:00'), updatedAt: dateAt(2, '17:44:00') },
  { _id: 12, articleId: 3, userId: 14, parentId: null, replyUserId: null, content: 'pointer-events: none 这个坑太真实了，当年被挡点击挡到怀疑人生', deleted: false, createdAt: dateAt(1, '09:15:00'), updatedAt: dateAt(1, '09:15:00') },
  { _id: 13, articleId: 1, userId: 14, parentId: null, replyUserId: null, content: '哼哼，本大人宣布：这个博客的配色勉强配得上本大人的品味（骄傲叉腰）', deleted: false, createdAt: dateAt(0, '10:02:00'), updatedAt: dateAt(0, '10:02:00') },
  { _id: 14, articleId: 4, userId: 11, parentId: 9, replyUserId: 15, content: '不会告诉你我的「弃了」栏里躺着 12 部……有生之年系列', deleted: false, createdAt: dateAt(0, '11:36:00'), updatedAt: dateAt(0, '11:36:00') },
]);

print('miskk_article 种子完成：article=' + db.article.countDocuments() +
    ' category=' + db.category.countDocuments() +
    ' tag=' + db.tag.countDocuments() +
    ' comment=' + db.comment.countDocuments());
