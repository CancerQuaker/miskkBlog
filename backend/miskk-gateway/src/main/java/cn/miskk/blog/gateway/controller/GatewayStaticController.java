package cn.miskk.blog.gateway.controller;

import cn.miskk.blog.gateway.handler.StaticHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

/**
 * 上传文件静态资源入口：/api/static/{user|article}/** → 对应服务的 /uploads/**。
 */
@Tag(name = "网关-静态资源")
@RestController
@RequestMapping("/api/static")
@RequiredArgsConstructor
public class GatewayStaticController {

    private final StaticHandler staticHandler;

    @Operation(summary = "用户服务上传文件")
    @GetMapping("/user/**")
    public ResponseEntity<byte[]> user(HttpServletRequest request) {
        return staticHandler.forward("miskk-user", remainder(request));
    }

    @Operation(summary = "博客服务上传文件")
    @GetMapping("/article/**")
    public ResponseEntity<byte[]> article(HttpServletRequest request) {
        return staticHandler.forward("miskk-blog", remainder(request));
    }

    /** 提取 ** 匹配的剩余路径，如 uploads/user/202610/x.png */
    private String remainder(HttpServletRequest request) {
        Object attr = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        return attr == null ? "" : attr.toString();
    }
}
