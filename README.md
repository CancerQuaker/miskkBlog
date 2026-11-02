# 迷失客客の博客 🐱

一个二次元风格的全栈博客系统：**Vite + React 18** 前端，**Spring Boot 3.3 单体**后端（单个应用，无注册中心），**PostgreSQL 单库 + Redis** 数据层，Knife4j 接口文档。

> ✨ 特色：魔法星星鼠标指针（拖尾 + 左键掉落文字彩蛋）、自定义右键菜单、Live2D 看板娘（仙狐小姐，本地模型）、樱花特效、亮/暗/护眼/跟随系统四主题、音乐播放器（网易云歌单 + 歌词同步）、文章评论点赞分享与在线时长统计、友链申请审核、ECharts 数据看板与系统资源监控。

---

## 目录结构

```
miskkBlog/
├── pom.xml                     # Maven 父 POM（仓库根目录，IDEA 直接打开根目录即可导入）
├── backend/
│   └── miskk-server/           # 唯一后端模块 :8100 —— 用户/文章/评论/友链/管理端（统计聚合/系统监控），PostgreSQL 单库
│       └── src/main/java/cn/miskk/blog/
│           ├── MiskkApplication.java   # 启动类
│           ├── common/         # Result/JWT/用户上下文/鉴权注解/DTO/VO/全局异常/工具
│           ├── web/            # 对前端 REST 层：controller(/api/**) → handler → service；AuthInterceptor 统一鉴权
│           ├── entity/ mapper/ service/ task/   # 全部业务（MyBatis-Plus；entity/mapper/service/task）+ 浏览量 30 秒落库任务
│           └── resources/application.yml
├── frontend/                   # 前端（Vite 6 + React 18 + TS + AntD 5）
├── sql/miskk_blog.sql          # PostgreSQL 建库脚本（全部 11 张表 + 种子，可重复执行）
└── docker/
    ├── docker-compose.yml      # 挂载本机已构建产物（jar/dist）启动全栈；亦可只起基础设施
    └── docker-compose.auto.yml # 一键编译+部署：拉代码即起全栈（容器内构建），见「生产部署」
```

## 技术栈

| 端 | 技术 |
|------|------|
| 前端 | Vite 6 · React 18 · TypeScript · Ant Design 5 · React Router 6 · ECharts · Axios · react-markdown · @uiw/react-md-editor · oh-my-live2d · zustand |
| 后端 | Spring Boot 3.3.13（单体）· MyBatis-Plus 3.5.17 · Hutool · jjwt · Knife4j · oshi |
| 数据 | PostgreSQL 16+（单库 miskk_blog：全部表统一存放，开发验证于 18）· Redis 7（验证码 / 浏览量缓冲 / 用户名片缓存） |

## 快速开始

### 0. 环境要求

- JDK 17+（本项目在 JDK 24 上验证编译通过）
- Node.js 18+（在 Node 24 验证）
- Maven 3.8+、PostgreSQL 15+（开发验证于 18.4）、Redis

### 1. 启动基础设施

有 Docker（`docker-compose.yml` 已含全套服务，本地开发只起这两个即可）：

```bash
cd docker
cp .env.example .env     # 本地开发建议把 POSTGRES_PASSWORD 设为 123456（与后端默认连接参数一致）、POSTGRES_USER 设为 postgres
# 编辑 docker-compose.yml，放开 db / redis 注释掉的 ports（后端要从宿主机连接）
docker compose up -d db redis    # PostgreSQL(5432) + Redis(6379)，首次启动自动初始化数据
```

无 Docker：本机安装 PostgreSQL / Redis 即可，不再需要任何注册中心。

### 2. 初始化数据库

PostgreSQL 默认连接参数为 `127.0.0.1:5432` / `postgres` / `123456`（库 `miskk_blog`），可用环境变量覆盖（见下文配置表）。

本机直连（无 Docker，或已放开容器端口映射）：

```bash
# 先建库（已存在则跳过），再执行脚本（建表 + 种子，可重复执行）
psql -U postgres -c "CREATE DATABASE miskk_blog ENCODING 'UTF8'"
psql -U postgres -d miskk_blog -f sql/miskk_blog.sql
```

> 脚本可重复执行：用户/友链/分类/标签幂等冲突跳过；种子文章与评论按固定 id 段删除后重灌（发布/评论时间按执行日相对计算）；
> 末尾会 `setval` 对齐各表 IDENTITY 序列（显式插入 id 不推进序列，不对齐则应用首次自增插入即主键冲突）。

数据跑在 Docker 容器里时（容器名 `miskk-pg`、账号密码即 `docker/.env` 里的 `POSTGRES_USER`/`POSTGRES_PASSWORD`；两个 compose 文件首次启动空数据卷时会自动执行 `sql/miskk_blog.sql`，无需手动）：

```bash
cd docker
docker exec -i miskk-pg psql -U miskk -d miskk_blog < ../sql/miskk_blog.sql

# 验证：应有 11 张表、4 篇种子文章
docker exec -it miskk-pg psql -U miskk -d miskk_blog -c "\dt" -c "SELECT count(*) FROM t_article;"
```

> `/docker-entrypoint-initdb.d` **只在数据卷为空时执行一次**：若容器曾用旧数据卷启动过、或初始化中途失败需要重跑，就用上面的命令手动执行。

### 3. 启动后端

在仓库根目录执行（父 POM 就在根目录）：

```bash
mvn clean install -DskipTests

mvn -pl backend/miskk-server spring-boot:run      # 或直接在 IDEA 中运行 MiskkApplication 主类
# 或起打好的包：java -jar backend/miskk-server/target/miskk-server-1.0.0.jar
```

只有一个进程，监听 `8100`，启动后即可访问 http://localhost:8100/doc.html 查看接口文档（需 PostgreSQL / Redis 均已就绪）。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev        # http://localhost:5173
```

Vite 开发服务器把 `/api` 代理到后端 `http://localhost:8100`。

### 5. 默认账号

| 用户名 | 密码 | 说明 |
|--------|------|------|
| admin | admin123 | 管理员（可进 `/admin` 后台） |

注册普通用户：邮箱验证码方式；**未配置 SMTP 时验证码会直接在响应与后端日志中返回**，方便本地调试。

## 配置说明（环境变量 / application.yml）

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `POSTGRES_ADDR` / `POSTGRES_USER` / `POSTGRES_PASSWORD` | `127.0.0.1:5432` / `postgres` / `123456` | PostgreSQL 连接（单库 miskk_blog） |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | `127.0.0.1` / `6379` / 空 | Redis |
| `JWT_SECRET` | 内置开发值 | 登录态 JWT 密钥（**生产必须替换**，≥32 字节；签发与解析都在本应用） |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_USER` / `MAIL_PASSWORD` | 空 / `465` / 空 / 空 | 配置后验证码真实发信；为空则调试模式 |
| `MAIL_DEBUG` | `true` | 未配置邮件时在响应中返回验证码 |
| `GITHUB_CLIENT_ID` / `GITHUB_CLIENT_SECRET` | 空 | GitHub OAuth 登录；为空则提示未开放 |
| `UPLOAD_DIR` | `~/.miskk/uploads` | 图片上传目录（头像存 `user/`、文章配图存 `article/` 子目录） |

> QQ / 微信登录需要企业资质才能开通互联平台，本项目已预留接口与提示（`/auth/qq`、`/auth/wechat`），GitHub OAuth 已完整实现，配置 Client-ID 后即可使用。

### 6. 生产部署（Ubuntu 云服务器等）

全容器一键部署：后端在容器内 Maven 多阶段构建，前端 npm build 后由 nginx 托管并反代 `/api`。服务器上只需装好 Docker（含 compose 插件）：

```bash
git clone https://github.com/CancerQuaker/miskkBlog.git
cd miskkBlog/docker
cp .env.example .env    # 必填 POSTGRES_PASSWORD / JWT_SECRET；端口/JVM 等可选变量见文件内说明
bash deploy.sh          # 等价于 docker compose -f docker-compose.auto.yml up -d --build
```

说明：

- **本机已有构建产物**时（根目录 `mvn clean install -DskipTests` + `frontend` 下 `npm run build`），可改用同目录 `docker-compose.yml`：不做容器内编译，直接挂载 `backend/miskk-server/target/*.jar` 与 `frontend/dist` 启动同一套全栈（路径可在 `.env` 用 `APP_JAR_DIR` / `FRONTEND_DIST_DIR` 改指别处；与 `.env` 通用；只想起基础设施时 `docker compose up -d db redis`）。
- **安全模型**：默认只有 nginx 暴露 `SITE_PORT`（默认 80）端口，PostgreSQL/Redis/后端都在 compose 内部网络（`docker-compose.yml` 另把后端映射到宿主机 `127.0.0.1:8100`，便于宿主机 nginx 反代），云安全组只需放行 80/443。所有端口（宿主机侧与容器内侧）都可在 `.env` 自定义，PostgreSQL/Redis 的宿主机映射在 compose 里以注释预留。
- **首次构建**约需几分钟（容器内编译）；国内服务器可在 `.env` 配 `MAVEN_MIRROR` / `NPM_REGISTRY` 镜像加速。
- **数据备份**：`pg-data` / `redis-data` 卷 + `docker/uploads/` 目录（上传图片）。
- **HTTPS**：宿主机再加一层 Caddy / certbot 终止证书，反代到 80 即可。
- **内存**：单个 Java 进程 + 单库，2核2G 即可从容运行（默认 mem_limit 基线：app 512m / PostgreSQL 384m / Redis 64m，约 1G）；更紧张时设 `JAVA_TOOL_OPTIONS=-Xmx256m`。
- 首次启动自动初始化数据（仅空数据卷时执行 `sql/miskk_blog.sql`；数据卷非空或失败时需手动执行，方法见「快速开始 → 2. 初始化数据库」的 Docker 部分）；默认管理员 `admin / admin123`，部署后请尽快改密。

## 接口文档（Knife4j）

启动后端后访问 http://localhost:8100/doc.html （按 认证 / 用户资料 / 文章 / 管理-文章 / 友链 / 管理-后台 分组）。

## 架构说明

```
                      ┌────────────────────────────────────────────────┐
   浏览器 ───────────▶│            miskk-server（单体 :8100）            │
   (Vite / nginx 反代) │                                                │
        /api/**       │  web.controller（/api/**，GET/PUT/DELETE）       │
                      │        │  AuthInterceptor：JWT → UserContext     │
                      │        ▼  白名单 / 401 / 403 / @RequireLogin     │
                      │  web.handler（参数翻译、Result 组装）             │
                      │        ▼                                       │
                      │  service ─ 用户 │ 友链 │ 文章 │ 评论 │ 互动 │ 统计 │ 监控(oshi)
                      │        │ MyBatis-Plus（PostgreSQL 方言，ILIKE 搜索）     │
                      └────────┼────────────────────────────────────────────────┘
                               ▼
                     PostgreSQL miskk_blog（单库，11 张表统一存放）
                      用户 │ 友链 │ 文章/分类/标签 │ 评论/点赞/分享/观看
                               └───────────────┬──────────────
                                 Redis（验证码 / 浏览量缓冲 / 名片缓存）
```

设计要点：

- **单进程、单入口**：前端所有请求走 `/api/**`，由同一个 Spring Boot 应用处理；没有注册中心、没有服务间 HTTP 调用，本地开发只需 PostgreSQL + Redis。
- **统一鉴权**：`AuthInterceptor` 解析 `Authorization: Bearer` JWT 并填充 `UserContext`（ThreadLocal，匿名也填充、userId 为 null）；白名单外的请求必须登录（401），`/api/admin/**` 与标注 `@RequireAdmin` 的接口必须管理员（403）。登录成功后由 `AuthHandler` 签发 JWT，密钥 `JWT_SECRET` 仅本应用持有。
- **浏览量削峰**：浏览经 Redis `SADD` 按访客按天去重 + Hash 缓冲增量，`ViewFlushTask` 每 30 秒批量落库；匿名访客按客户端 IP 哈希标识。
- **在线观看时长**：前端每 20 秒心跳上报（页面可见时），离开页面用 `sendBeacon` 兜底；按访客+日期聚合。
- **PostgreSQL 单库**：全部 11 张表统一放在 `miskk_blog` 一个库（用户/友链/文章/分类/标签/评论/点赞/分享/观看记录），MyBatis-Plus 访问；跨业务写操作共享同一个数据库事务，备份只需 dump 一个库。主键用 `GENERATED BY DEFAULT AS IDENTITY`（种子显式插 id 后脚本末尾 `setval` 对齐序列）；文章/评论搜索用 `ILIKE` 保持不区分大小写；点赞 `(article_id, user_id)` 唯一索引防并发重复。
- **上传静态资源**：图片落盘到 `UPLOAD_DIR` 的 `user/` / `article/` 子目录，经 `/api/static/user/uploads/**` 与 `/api/static/article/uploads/**` 直接映射对外访问。

## 前端设计说明

- 主题：`亮色 / 暗色 / 护眼 / 跟随系统`（默认主题色草绿 `#80a942`），AntD token + CSS 变量双轨同步；导航栏「外观」面板支持自定义主题色（8 个预设 + 取色器）与特效开关（樱花飘落 / 星星鼠标 / 看板娘），全部持久化。
- 星星光标 `StarCursor`：桌面端隐藏系统光标，旋转四角星跟随鼠标并沿途洒落拖尾小星星，左键掉落「富强/民主/文明/和谐…」彩色文字；右键呼出自定义右键菜单（`ContextMenu`）。
- 移动端适配：窄屏下导航精简（搜索/主题转图标、渐变胶囊登录按钮、汉堡抽屉菜单）、隐藏站名与看板娘、卡片单列布局。
- 看板娘：`oh-my-live2d` 加载本地模型「仙狐小姐」（`frontend/public/live2d/senko/`），视线跟随鼠标、闲置语录轮播；摸头睡觉、双击唱歌、点身体触发动作（由模型 `senko.model3.json` 命中区定义），移动端不加载。
- 音乐播放器：全局单例 `<audio>`，导航栏入口 + 悬浮面板；默认通过 Meting API 拉取网易云歌单（知更鸟《空气中的听觉幻术》，主 API 失败自动切换备用），支持歌词同步 / 播放模式 / 封面 / 音量记忆，可在 `src/config/site.ts` 切换为内置本地歌单。

## 致谢

- 前端布局与组件设计参考开源主题 [CuteLeaf/Firefly](https://github.com/CuteLeaf/Firefly)（MIT，基于 [fuwari](https://github.com/saicaca/fuwari)），本项目为 React + 后端驱动的独立实现。
- 后端代码为本项目全新编写，未参考任何现成博客系统源码。
