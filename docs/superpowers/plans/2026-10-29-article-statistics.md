# 文章互动统计修正 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修正浏览、评论、分享的并发计数与页面刷新，以纯前端的本次阅读时长替代累计观看时长并移除其全部存储链路。

**Architecture:** 保留现有 Redis 浏览缓冲和文章表互动计数，用 Redis Lua 原子取出浏览增量、SQL 原子增减避免并发覆盖；阅读时长仅由文章详情页按可见时间计算。网关与下游继续遵守各自接口风格，累计时长相关接口和模型全部删除。

**Tech Stack:** Java 17、Spring Boot 3.3、MyBatis-Plus、Spring Data Redis、React 18、TypeScript、Vite 6、MySQL 8。

**Spec:** `docs/superpowers/specs/2026-10-29-article-statistics-design.md`

## Global Constraints

- 后端对外服务接口保持 POST + DTO，网关保持原 GET/POST/DELETE 风格。
- 浏览量仍按访客和日期去重，30 秒批量落库；评论总数包含回复，评论区分页 total 仍为顶级评论数。
- 当前阅读时长只在页面可见时累计，不上报、不持久化；保留预计阅读分钟数。
- 不为博客服务或前端新增测试脚手架；使用项目已有构建命令及真实接口/页面验证。
- SQL 迁移脚本只提供，不自动连接或修改现有数据库。
- 每个任务完成后本地提交，不 push。

## Review Focus

- 并发浏览落库、评论和分享不得互相覆盖：Task 1 用原子 Redis/SQL 操作，Task 4 做并发接口检查。
- flush 读取期间新增的浏览不得被删除：Task 1 用单条 Lua 原子 HGET+HDEL，并检查异常时重新写回缓冲。
- 删除顶级评论必须扣除全部回复且不能减成负数：Task 1 使用实际删除数和 `GREATEST`，Task 4 验证界面与数据库计数。
- 弹窗拦截、二维码生成或剪贴板失败不得增加分享数：Task 2 先完成动作再上报，Task 4 验证失败路径。
- 页面隐藏、文章切换和组件卸载不得多计本次阅读时长：Task 2 以时间戳和可见性事件计算，Task 4 做页面验证。

---

### Task 1: 后端计数修正与累计时长清理

**Files:**
- Modify: `backend/miskk-service-blog/src/main/java/cn/miskk/blog/mapper/ArticleMapper.java`
- Modify: `backend/miskk-service-blog/src/main/java/cn/miskk/blog/service/impl/ArticleServiceImpl.java`
- Modify: `backend/miskk-service-blog/src/main/java/cn/miskk/blog/service/impl/CommentServiceImpl.java`
- Modify: `backend/miskk-service-blog/src/main/java/cn/miskk/blog/service/impl/InteractionServiceImpl.java`
- Modify: `backend/miskk-service-blog/src/main/java/cn/miskk/blog/service/InteractionService.java`
- Modify: `backend/miskk-service-blog/src/main/java/cn/miskk/blog/controller/InteractionController.java`
- Modify: `backend/miskk-service-blog/src/main/java/cn/miskk/blog/entity/Article.java`
- Modify: `backend/miskk-common/src/main/java/cn/miskk/blog/common/vo/ArticleCardVO.java`
- Modify: `backend/miskk-common/src/main/java/cn/miskk/blog/common/vo/ArticleDetailVO.java`
- Modify: `backend/miskk-common/src/main/java/cn/miskk/blog/common/auth/LoginUser.java`
- Modify: `backend/miskk-gateway/src/main/java/cn/miskk/blog/gateway/controller/GatewayArticleController.java`
- Modify: `backend/miskk-gateway/src/main/java/cn/miskk/blog/gateway/handler/ArticleHandler.java`
- Modify: `backend/miskk-gateway/src/main/java/cn/miskk/blog/gateway/service/BlogRemoteService.java`
- Modify: `backend/miskk-gateway/src/main/java/cn/miskk/blog/gateway/interceptor/GatewayAuthInterceptor.java`
- Modify: `backend/miskk-service-blog/pom.xml`
- Delete: `backend/miskk-common/src/main/java/cn/miskk/blog/common/dto/WatchDTO.java`
- Delete: `backend/miskk-service-blog/src/main/java/cn/miskk/blog/entity/WatchRecord.java`
- Delete: `backend/miskk-service-blog/src/main/java/cn/miskk/blog/mapper/WatchRecordMapper.java`

**Interfaces:**
- Produces: `ArticleMapper.incrementViews(Long id, long delta)`、`incrementComments(Long id)`、`decrementComments(Long id, long delta)`、`incrementShares(Long id)`，均返回受影响行数。
- Produces: 文章详情 `views` 为数据库计数加当前 Redis 待落库增量；互动接口不再存在 `/watch` 或 `watchSeconds`。
- Consumes: 现有 `recordView(Long articleId, String visitorIdentity): boolean` 和 `share(Long articleId, String channel, Long userId): boolean` 签名保持不变。

- [ ] **Step 1: 增加原子计数 SQL**
  在 `ArticleMapper` 用 `@Update` 和 `@Param` 定义四个计数方法；增量直接执行 `column = column + delta`，评论减量使用 `GREATEST(comments - delta, 0)`，并限定 `id` 与 `deleted = 0`。
- [ ] **Step 2: 修正浏览缓冲和详情计数**
  `flushViews()` 先枚举 Hash 字段，再对每字段执行 Lua 原子 HGET+HDEL；数据库更新失败时把该增量重新写回 Hash 并记录日志。详情返回前读取该文章待落库增量并加到 `views`，空值按零处理。
- [ ] **Step 3: 修正评论和分享计数**
  创建评论、删除评论和分享成功后在原事务内调用原子计数方法；删除顶级评论仍按自身加实际回复数减量，不再读取旧值后覆盖。
- [ ] **Step 4: 删除累计观看时长链路**
  删除上述 DTO、实体、Mapper 和 `/watch` 各层方法，移除公开路径、VO/实体字段、初始化赋值、相关 import 与文档注释；博客模块描述去掉“时长”。
- [ ] **Step 5: 构建验证**
  在仓库根目录运行 `mvn clean install -DskipTests`，预期所有模块 `BUILD SUCCESS`；再搜索 `WatchDTO|WatchRecord|watchSeconds|/watch`，预期业务代码无匹配。
- [ ] **Step 6: 提交**
  提交设计文档、计划和 Task 1 后端改动，提交信息为 `feat: 修正文章互动计数并移除累计时长后端`。

### Task 2: 前端即时统计与本次阅读计时

**Files:**
- Modify: `frontend/src/pages/ArticleDetailPage.tsx`
- Modify: `frontend/src/components/CommentSection.tsx`
- Modify: `frontend/src/components/ShareButtons.tsx`
- Modify: `frontend/src/components/Common.tsx`
- Modify: `frontend/src/api/article.ts`

**Interfaces:**
- Produces: `CommentSection({ articleId, onCountChange })`，其中 `onCountChange(delta: number)` 在新增或删除成功后报告变化量。
- Produces: `ShareButtons({ articleId, title, onShared })`，其中 `onShared(): void` 仅在上报成功后调用。
- Consumes: Task 1 保留的 `view` 返回 `boolean`、`share` 返回 `void`；不再存在 `articleApi.watch` 或 `watchSeconds` 类型字段。

- [ ] **Step 1: 实现可见性感知的本次阅读计时**
  在文章详情用 state 展示秒数、ref 保存累计毫秒和可见段起点；按 `Date.now()` 结算而不是假设定时器准时运行。监听 `visibilitychange`，隐藏暂停、恢复继续，切换文章清零，卸载时移除定时器和事件；显示“当前阅读 {fmtDuration(...)}”。
- [ ] **Step 2: 同步浏览、评论和分享数字**
  浏览接口返回 `true` 时以函数式 state 更新当前文章；评论区新增传 `+1`，删除回复传 `-1`，删除顶级评论传 `-(1 + children.length)`；分享成功传 `+1`。所有更新不得切换文章后修改旧文章数据。
- [ ] **Step 3: 只在有效分享动作后上报**
  QQ 先确认分享窗口成功打开，微信先生成并展示二维码，复制链接先等待剪贴板成功，再调用分享接口；失败不调用 `onShared`，并给用户明确错误提示。
- [ ] **Step 4: 移除累计时长展示与 API**
  删除文章卡片时长、`watchSeconds` 类型和 `articleApi.watch`，保留 `fmtDuration` 供本次阅读使用，并清理不再使用的 import。
- [ ] **Step 5: 构建验证**
  在 `frontend` 运行 `npm run build`，预期 TypeScript 和 Vite 均成功；搜索前端 `watchSeconds|articleApi.watch|/watch|累计观看`，预期无匹配。
- [ ] **Step 6: 提交**
  提交 Task 2 改动，提交信息为 `feat: 完善文章统计展示与当前阅读计时`。

### Task 3: 数据库迁移与文档同步

**Files:**
- Create: `sql/04_remove_watch_stats.sql`
- Modify: `sql/02_miskk_article.sql`
- Modify: `sql/README.md`
- Modify: `README.md`
- Modify: `AGENTS.md`
- Modify: `docs/superpowers/specs/2026-10-29-article-statistics-design.md`

**Interfaces:**
- Produces: 新安装数据库不再包含 `watch_seconds` 和 `t_watch_record`；已有数据库可手工执行 `sql/04_remove_watch_stats.sql` 完成同样清理。
- Consumes: Task 1 和 Task 2 已完成累计时长代码清理。

- [ ] **Step 1: 更新初始化与种子 SQL**
  从 `t_article`、种子 INSERT 列和值、重复更新字段中删除累计时长，并删除 `t_watch_record` 建表语句；保持四篇种子文章其余计数不变。
- [ ] **Step 2: 提供独立迁移**
  `04_remove_watch_stats.sql` 使用 `miskk_article` 库，删除 `watch_seconds` 并以 `DROP TABLE IF EXISTS` 删除 `t_watch_record`；文件注释说明会永久删除历史累计时长且不会自动执行。
- [ ] **Step 3: 同步说明**
  SQL README 增加已有库迁移命令；README 和 AGENTS.md 将心跳/累计时长链路改为“当前阅读时长纯前端按可见时间统计、不上报”，保留浏览量 Redis 说明；设计文档标注已按确认方案实施。
- [ ] **Step 4: 静态验证**
  搜索全仓库 `WatchDTO|WatchRecord|watchSeconds|watch_seconds|t_watch_record|累计观看|在线观看时长`，除迁移文件及必要的历史设计说明外无残留；检查种子 SQL 各 INSERT 列数与值数一致。
- [ ] **Step 5: 提交**
  提交 Task 3 改动，提交信息为 `chore: 移除累计观看时长数据库结构与文档`。

### Task 4: 端到端验证与最终检查

**Files:**
- Modify only if verification exposes a defect in the files above.

**Interfaces:**
- Consumes: Tasks 1–3 的完整前后端和 SQL 改动。
- Produces: 可复现的验证结果及最终干净的工作区（除已提交改动外无测试产物）。

- [ ] **Step 1: 重跑标准构建**
  根目录运行 `mvn clean install -DskipTests`，随后运行 `mvn -pl backend/miskk-gateway test` 和 `cd frontend && npm run build`，预期全部通过。
- [ ] **Step 2: 验证真实互动接口**
  在本地依赖和服务可用时，通过网关实际请求文章详情、浏览、分享、评论新增/删除；确认同日重复浏览不重复计数、并发计数无覆盖、评论和分享数字与日志一致。服务不可用时记录具体阻塞，不把编译当作接口验证。
- [ ] **Step 3: 验证真实阅读页面**
  使用浏览器打开文章详情，确认浏览/评论/分享操作后数字即时变化；阅读秒数可见时增长、切到其他标签页暂停、返回继续、切换文章清零；分享失败不增加计数。
- [ ] **Step 4: 清理与最终提交**
  检查 `git status --short`，删除本次验证产生且不需要的日志或构建临时文件（保留项目既有 `dist` 产物状态）；若有验证修复则构建后提交 `fix: 修正文章统计验证发现的问题`，最后报告迁移脚本未自动执行及任何未能实测的项目。
