# SQL 执行说明

按顺序在 MySQL 8 中执行（可用 Navicat / IDEA Database 面板 / 命令行）：

```bash
mysql -uroot -p < sql/01_miskk_user.sql
mysql -uroot -p < sql/02_miskk_article.sql
mysql -uroot -p < sql/03_miskk_friend.sql
```

## 管理员账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员（role=1） |

> `01_miskk_user.sql` 中的种子管理员密码哈希由 Hutool BCrypt 生成（见脚本注释）。
> 若哈希与你使用的 Hutool 版本不兼容（几乎不会），用 Hutool 的
> `BCrypt.hashpw("admin123")` 重新生成并替换即可。

## 演示用户账号

`01_miskk_user.sql` 另含 5 个演示用户（id 11-15，密码统一 `admin123`，复用管理员的 BCrypt 哈希），
供 `02_miskk_article.sql` 的种子评论（近 14 天、含楼中楼回复）展示评论与后台评论趋势图使用：

| 用户名 | 昵称 |
|--------|------|
| xiaoxing | 小星尘 |
| paimon | 应急食品派蒙 |
| stelle | 三月七 |
| funina | 芙宁娜 |
| yinlang | 银狼 |

## 注意

- 三个库保持独立：用户服务连 `miskk_user`；博客服务（文章/友链/管理合并）连 `miskk_article`（主）与 `miskk_friend`（dynamic-datasource `@DS("friend")` 路由）。
- 种子文章的 `author_id = 1`，对应种子管理员；若你重建了用户表，请同步修改。
- 重复执行 `02_miskk_article.sql` 会刷新种子文章（含浏览/点赞等计数）并重建种子评论/标签（仅限 id 1-4 的文章与 id 1-14 的评论），线上真实数据不受影响；发布/评论时间按执行日相对计算（近 30/14 天摊开）。
- 修改密码也可登录后在「个人资料」中自助修改。
