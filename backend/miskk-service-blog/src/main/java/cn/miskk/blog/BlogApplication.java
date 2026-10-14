package cn.miskk.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 博客服务启动类：文章（分类/标签/评论/点赞/浏览/分享/时长）
 * + 友链（展示/申请/审核）+ 管理端（仪表盘统计/系统监控）。
 * <p>
 * 代码按层平铺：controller / service / mapper / entity / task，
 * Feign 远程接口与网关做法一致放在 service 包（UserRemoteService），
 * 默认扫描 cn.miskk.blog 整棵包树（含 common 公共配置）。
 * <p>
 * 双数据源：miskk_article（主）+ miskk_friend，友链侧通过 @DS("friend") 路由；
 * 用户名片/统计通过 Feign 调用用户服务内部接口。
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "cn.miskk.blog.service")
@EnableScheduling
public class BlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}
