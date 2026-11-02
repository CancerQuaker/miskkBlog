package cn.miskk.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 迷失客客の博客 —— 单体后端启动类（:8100）。
 * <p>
 * 请求链路：web.controller（对前端 REST，/api/**）→ web.handler（参数翻译/组装）→ service → 数据层；
 * 鉴权由 web.interceptor.AuthInterceptor 统一处理（JWT 解析 + 白名单 + @RequireLogin/@RequireAdmin）；
 * 数据层：PostgreSQL 单库 miskk_blog（MyBatis-Plus，全部表统一存放，用户/友链/文章域）；
 * 定时任务：task.ViewFlushTask 每 30 秒把 Redis 缓冲的浏览量落库。
 */
@SpringBootApplication
@EnableScheduling
public class MiskkApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiskkApplication.class, args);
    }
}
