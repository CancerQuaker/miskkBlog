# SQL 执行说明

数据层双栈：**MySQL 存基础数据（用户 / 友链）**，**MongoDB 存文章全家族（文章 / 分类 / 标签 / 评论 / 点赞 / 分享 / 观看记录）**。

```
sql/
├── mysql/miskk_base.sql     # MySQL 基础库建表 + 种子（用户、友链）
└── mongo/miskk_article.js   # MongoDB 文章库种子（mongosh 脚本；索引由后端启动时自动创建）
```

## MySQL 基础库

```bash
mysql -uroot -p --default-character-set=utf8mb4 < sql/mysql/miskk_base.sql
```

（docker-compose 的 MySQL 容器首次启动会自动执行该脚本；容器内手动执行见 `docker exec -it miskk-mysql sh` 后
`mysql -uroot -p < /docker-entrypoint-initdb.d/miskk_base.sql`。）

## MongoDB 文章库

需要 mongosh（本机安装：`npm install -g mongosh`；docker 的 mongo 镜像自带，容器首次启动自动执行种子脚本）：

```bash
mongosh "mongodb://127.0.0.1:27017" --file sql/mongo/miskk_article.js
```

集合：`article` / `category` / `tag` / `comment` / `article_like` / `share_log` / `watch_record`。
**索引不在脚本里建**（与后端 `@Indexed/@CompoundIndex` 注解定义的索引名冲突会报 IndexOptionsConflict），
后端首次启动时自动创建（`spring.data.mongodb.auto-index-creation=true`），包括
`article_like` 的 `(articleId, userId)` 唯一索引与 `tag.name` 唯一索引。

## 管理员账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员（role=1） |

> `miskk_base.sql` 中的种子管理员密码哈希由 Hutool BCrypt 生成（见脚本注释）。
> 若哈希与你使用的 Hutool 版本不兼容（几乎不会），用 Hutool 的
> `BCrypt.hashpw("admin123")` 重新生成并替换即可。

## 演示用户账号

`miskk_base.sql` 另含 5 个演示用户（id 11-15，密码统一 `admin123`，复用管理员的 BCrypt 哈希），
供 MongoDB 种子评论（近 14 天、含楼中楼回复）展示评论与后台评论趋势图使用：

| 用户名 | 昵称 |
|--------|------|
| xiaoxing | 小星尘 |
| paimon | 应急食品派蒙 |
| stelle | 三月七 |
| funina | 芙宁娜 |
| yinlang | 银狼 |

## 注意

- 两边脚本都**可重复执行**：MySQL 用 `ON DUPLICATE KEY` 幂等；Mongo 脚本先删除固定 id 段（文章 1-4、评论 1-14、分类 1-3、标签 1-6）再写入，不影响种子之外的数据。Mongo 脚本会重置种子文章的浏览/点赞等计数与发布时间（按执行日相对计算，近 30 天摊开）。
- 种子文章的 `authorId = 1`，对应种子管理员；若你重建了用户表，请同步修改。
- 文章/评论 ID 在 Mongo 中仍为 Long：种子用固定小 ID，应用新建的用雪花算法（`IdUtil.getSnowflakeNextId()`）。
- 修改密码也可登录后在「个人资料」中自助修改。
