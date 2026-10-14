package cn.miskk.blog.gateway.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 网关 HTTP 配置：仅提供静态资源转发所需的负载均衡 RestTemplate。
 * <p>
 * Feign 调用的用户上下文（X-User-Id / X-User-Role / X-Forwarded-For）
 * 由各 FeignClient 接口以 @RequestHeader 参数形式显式传递，无需额外配置。
 */
@Configuration
public class GatewayHttpConfig {

    /** 服务发现负载均衡的 RestTemplate，用于 /api/static/** 字节流转发 */
    @Bean
    @LoadBalanced
    public RestTemplate loadBalancedRestTemplate() {
        return new RestTemplate();
    }
}
