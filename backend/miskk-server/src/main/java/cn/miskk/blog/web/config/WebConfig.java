package cn.miskk.blog.web.config;

import cn.miskk.blog.web.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC 配置：统一鉴权拦截器 + 上传文件静态映射。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final String uploadDir;

    public WebConfig(AuthInterceptor authInterceptor,
                     @Value("${miskk.upload.dir:${user.home}/.miskk/uploads}") String uploadDir) {
        this.authInterceptor = authInterceptor;
        this.uploadDir = uploadDir.endsWith("/") || uploadDir.endsWith("\\") ? uploadDir : uploadDir + "/";
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .order(0);
    }

    /**
     * 上传图片访问路径：/api/static/user/uploads/** 与 /api/static/article/uploads/**
     * 均映射到同一上传目录（UploadUtil 按 user / article 子目录落盘，URL 中 uploads/ 之后即目录内相对路径）。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api/static/user/uploads/**", "/api/static/article/uploads/**")
                .addResourceLocations("file:" + uploadDir);
    }
}
