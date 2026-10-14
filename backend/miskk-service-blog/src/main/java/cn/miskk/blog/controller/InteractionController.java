package cn.miskk.blog.controller;

import cn.miskk.blog.common.dto.IdDTO;
import cn.miskk.blog.common.dto.ShareDTO;
import cn.miskk.blog.common.dto.WatchDTO;
import cn.miskk.blog.service.ArticleService;
import cn.miskk.blog.service.InteractionService;
import cn.miskk.blog.service.LikeService;
import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.RequireLogin;
import cn.miskk.blog.common.auth.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

/**
 * 互动接口：浏览/点赞/分享/观看时长（浏览、分享、观看允许匿名）。
 * 文章 ID 一律从请求体 DTO 获取，不使用路径参数。
 */
@Tag(name = "互动")
@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class InteractionController {

    private final LikeService likeService;
    private final InteractionService interactionService;
    private final ArticleService articleService;

    @Operation(summary = "上报一次浏览（同一访客每日仅计一次）")
    @PostMapping("/view")
    public Result<Boolean> view(@Valid @RequestBody IdDTO dto, HttpServletRequest request) {
        return Result.ok(articleService.recordView(dto.getId(), visitorIdentity(request)));
    }

    @Operation(summary = "点赞（需登录，每人仅一次）")
    @RequireLogin
    @PostMapping("/like")
    public Result<Boolean> like(@Valid @RequestBody IdDTO dto) {
        boolean first = likeService.like(dto.getId(), UserContext.getUserId());
        return Result.ok(first);
    }

    @Operation(summary = "取消点赞")
    @RequireLogin
    @PostMapping("/unlike")
    public Result<Boolean> unlike(@Valid @RequestBody IdDTO dto) {
        return Result.ok(likeService.unlike(dto.getId(), UserContext.getUserId()));
    }

    @Operation(summary = "分享至 QQ / 微信 / 复制链接")
    @PostMapping("/share")
    public Result<Void> share(@Valid @RequestBody ShareDTO dto) {
        Long userId = UserContext.get() == null ? null : UserContext.getUserId();
        interactionService.share(dto.getArticleId(), dto.getChannel(), userId);
        return Result.ok();
    }

    @Operation(summary = "上报在线观看时长（心跳）")
    @PostMapping("/watch")
    public Result<Void> watch(@Valid @RequestBody WatchDTO dto, HttpServletRequest request) {
        Long userId = UserContext.get() == null ? null : UserContext.getUserId();
        interactionService.watch(dto.getArticleId(), dto.getSeconds(), userId, visitorIdentity(request));
        return Result.ok();
    }

    private String visitorIdentity(HttpServletRequest request) {
        Long userId = UserContext.get() == null ? null : UserContext.getUserId();
        if (userId != null) {
            return "u:" + userId;
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((ip + "|miskk-salt").getBytes(StandardCharsets.UTF_8));
            return "ip:" + HexFormat.of().formatHex(digest, 0, 8);
        } catch (Exception e) {
            return "ip:" + ip;
        }
    }
}
