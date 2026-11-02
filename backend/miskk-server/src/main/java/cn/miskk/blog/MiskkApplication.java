package cn.miskk.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 迷失客客の博客 —— 单体后端启动类（:8100）。
 * <p>
 * 请求链路：web.controller（对前端 REST，/api/**）→ web.handler（参数翻译/组装）→ service → 数据层；
 * 鉴权由 web.interceptor.AuthInterceptor 统一处理（JWT 解析 + 白名单 + @RequireLogin/@RequireAdmin）；
 * 数据层双栈：MySQL（miskk_base，MyBatis-Plus：用户/友链）+ MongoDB（miskk_article，Spring Data Mongo：文章/分类/标签/评论/互动）；
 * 定时任务：task.ViewFlushTask 每 30 秒把 Redis 缓冲的浏览量落库。
 */
@SpringBootApplication
@EnableScheduling
public class MiskkApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiskkApplication.class, args);
    }
}
