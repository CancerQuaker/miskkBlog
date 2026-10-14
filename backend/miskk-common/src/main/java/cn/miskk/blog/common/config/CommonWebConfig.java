package cn.miskk.blog.common.config;

import cn.miskk.blog.common.auth.AuthInterceptor;
import cn.miskk.blog.common.auth.InternalTokenInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 服务通用 MVC 配置：拦截器链 + 本地上传文件静态映射。
 */
@Configuration
public class CommonWebConfig implements WebMvcConfigurer {

    private final String uploadDir;
    private final String internalToken;

    public CommonWebConfig(@Value("${miskk.upload.dir:${user.home}/.miskk/uploads}") String uploadDir,
                           @Value("${miskk.internal-token:miskk-internal-token-dev}") String internalToken) {
        this.uploadDir = uploadDir.endsWith("/") || uploadDir.endsWith("\\") ? uploadDir : uploadDir + "/";
        this.internalToken = internalToken;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 服务间内部接口：校验内部令牌
        registry.addInterceptor(new InternalTokenInterceptor(internalToken))
                .addPathPatterns("/internal/**")
                .order(0);
        // 登录态与权限校验
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/uploads/**")
                .order(10);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir);
    }
}
