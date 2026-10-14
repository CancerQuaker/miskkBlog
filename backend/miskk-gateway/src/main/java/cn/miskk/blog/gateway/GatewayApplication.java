package cn.miskk.blog.gateway;

import cn.miskk.blog.common.auth.JwtUtil;
import cn.miskk.blog.common.config.JacksonConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

/**
 * API 网关启动类。
 * <p>
 * 网关为 Servlet MVC 栈：controller（原请求方式）→ handler（参数翻译/转发）
 * → service（FeignClient，POST）→ miskk-user / miskk-blog。
 * 引入 common 中的 JwtUtil 与 JacksonConfig（与下游一致的日期序列化格式，
 * 否则 Feign 解码下游返回的 LocalDateTime 字段会失败），不扫描 common 包
 * （其中 CommonWebConfig / MybatisPlusConfig 是面向下游服务的配置）。
 */
@SpringBootApplication
@Import({JwtUtil.class, JacksonConfig.class})
@EnableDiscoveryClient
@EnableFeignClients
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
