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
> 若哈希与你使用的 Hutool 版本不兼容（几乎不会），可运行项目根目录
> `tools/GenBcryptHash.java` 重新生成并替换。

## 注意

- 三个库保持独立：用户服务连 `miskk_user`；博客服务（文章/友链/管理合并）连 `miskk_article`（主）与 `miskk_friend`（dynamic-datasource `@DS("friend")` 路由）。
- 种子文章的 `author_id = 1`，对应种子管理员；若你重建了用户表，请同步修改。
- 修改密码也可登录后在「个人资料」中自助修改。
