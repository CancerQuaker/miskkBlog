package cn.miskk.blog.gateway.config;

import cn.miskk.blog.gateway.interceptor.GatewayAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 网关 MVC 配置：注册统一鉴权拦截器。
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final GatewayAuthInterceptor gatewayAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(gatewayAuthInterceptor)
                .addPathPatterns("/**")
                .order(0);
    }
}
