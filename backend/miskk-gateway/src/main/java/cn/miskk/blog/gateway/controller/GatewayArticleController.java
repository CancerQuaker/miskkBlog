package cn.miskk.blog.gateway.controller;

import cn.miskk.blog.common.api.PageVO;
import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.dto.CommentCreateDTO;
import cn.miskk.blog.common.dto.ShareDTO;
import cn.miskk.blog.common.dto.WatchDTO;
import cn.miskk.blog.common.vo.ArticleCardVO;
import cn.miskk.blog.common.vo.ArticleDetailVO;
import cn.miskk.blog.common.vo.CategoryVO;
import cn.miskk.blog.common.vo.CommentVO;
import cn.miskk.blog.common.vo.TagVO;
import cn.miskk.blog.gateway.handler.ArticleHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文章/评论/分类/标签/上传入口（对前端保持原请求方式：GET 查询、POST 互动、DELETE 取消），
 * 请求/响应对象取自 common 共享 DTO/VO。
 */
@Tag(name = "网关-文章")
@RestController
@RequiredArgsConstructor
public class GatewayArticleController {

    private final ArticleHandler articleHandler;

    // ==================== 文章浏览 ====================

    @Operation(summary = "文章分页列表（按发布时间倒序，置顶优先）")
    @GetMapping("/api/article/articles")
    public Result<PageVO<ArticleCardVO>> page(@RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "10") long size,
                                              @RequestParam(required = false) Long categoryId,
                                              @RequestParam(required = false) Long tagId,
                                              @RequestParam(required = false) String keyword) {
        return articleHandler.page(page, size, categoryId, tagId, keyword);
    }

    @Operation(summary = "文章详情")
    @GetMapping("/api/article/articles/{id}")
    public Result<ArticleDetailVO> detail(@PathVariable Long id) {
        return articleHandler.detail(id);
    }

    @Operation(summary = "全文搜索")
    @GetMapping("/api/article/search")
    public Result<PageVO<ArticleCardVO>> search(@RequestParam String keyword,
                                                @RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "10") long size) {
        return articleHandler.search(keyword, page, size);
    }

    // ==================== 互动 ====================

    @Operation(summary = "上报一次浏览（同一访客每日仅计一次）")
    @PostMapping("/api/article/articles/{id}/view")
    public Result<Boolean> view(@PathVariable Long id) {
        return articleHandler.view(id);
    }

    @Operation(summary = "点赞（需登录，每人仅一次）")
    @PostMapping("/api/article/articles/{id}/like")
    public Result<Boolean> like(@PathVariable Long id) {
        return articleHandler.like(id);
    }

    @Operation(summary = "取消点赞")
    @DeleteMapping("/api/article/articles/{id}/like")
    public Result<Boolean> unlike(@PathVariable Long id) {
        return articleHandler.unlike(id);
    }

    @Operation(summary = "分享至 QQ / 微信 / 复制链接")
    @PostMapping("/api/article/articles/{id}/share")
    public Result<Void> share(@PathVariable Long id, @RequestBody ShareDTO dto) {
        return articleHandler.share(id, dto);
    }

    @Operation(summary = "上报在线观看时长（心跳）")
    @PostMapping("/api/article/articles/{id}/watch")
    public Result<Void> watch(@PathVariable Long id, @RequestBody WatchDTO dto) {
        return articleHandler.watch(id, dto);
    }

    // ==================== 评论 ====================

    @Operation(summary = "评论列表（树形，按时间倒序）")
    @GetMapping("/api/article/comments")
    public Result<PageVO<CommentVO>> comments(@RequestParam Long articleId,
                                              @RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "10") long size) {
        return articleHandler.comments(articleId, page, size);
    }

    @Operation(summary = "发表评论/回复（需登录）")
    @PostMapping("/api/article/comments")
    public Result<CommentVO> createComment(@RequestBody CommentCreateDTO dto) {
        return articleHandler.createComment(dto);
    }

    @Operation(summary = "删除自己的评论")
    @DeleteMapping("/api/article/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        return articleHandler.deleteComment(id);
    }

    // ==================== 分类 / 标签 ====================

    @Operation(summary = "分类列表（含已发布文章数）")
    @GetMapping("/api/article/categories")
    public Result<List<CategoryVO>> categories() {
        return articleHandler.categories();
    }

    @Operation(summary = "标签列表（含文章数）")
    @GetMapping("/api/article/tags")
    public Result<List<TagVO>> tags() {
        return articleHandler.tags();
    }

    // ==================== 图片上传 ====================

    @Operation(summary = "文章配图上传")
    @PostMapping("/api/article/upload/image")
    public Result<String> uploadImage(@RequestPart("file") MultipartFile file) {
        return articleHandler.uploadImage(file);
    }
}
