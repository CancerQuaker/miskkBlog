package cn.miskk.blog.gateway.handler;

import cn.miskk.blog.common.api.PageVO;
import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.UserContext;
import cn.miskk.blog.common.dto.ArticleSaveDTO;
import cn.miskk.blog.common.dto.ArticleStatusDTO;
import cn.miskk.blog.common.dto.CommentCreateDTO;
import cn.miskk.blog.common.dto.CommentQueryDTO;
import cn.miskk.blog.common.dto.IdDTO;
import cn.miskk.blog.common.dto.PageQueryDTO;
import cn.miskk.blog.common.dto.ShareDTO;
import cn.miskk.blog.common.dto.WatchDTO;
import cn.miskk.blog.common.vo.ArticleCardVO;
import cn.miskk.blog.common.vo.ArticleDetailVO;
import cn.miskk.blog.common.vo.CategoryVO;
import cn.miskk.blog.common.vo.CommentVO;
import cn.miskk.blog.common.vo.TagVO;
import cn.miskk.blog.gateway.service.BlogRemoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文章请求处理：把前端原有的 GET query / PUT / DELETE 请求翻译为下游 POST + 共享 DTO；
 * 用户上下文以单个 X-User-Info 请求头（LoginUser JSON）下发，
 * 匿名请求 userId 为 null，由下游 AuthInterceptor 还原为 UserContext。
 */
@Component
@RequiredArgsConstructor
public class ArticleHandler {

    private final BlogRemoteService blogRemoteService;

    // ==================== 文章浏览 ====================

    /** 文章分页列表：GET query → PageQueryDTO（登录管理员可见草稿/隐藏） */
    public Result<PageVO<ArticleCardVO>> page(long page, long size, Long categoryId, Long tagId, String keyword) {
        return blogRemoteService.articles(UserContext.toJson(),
                toQuery(page, size, categoryId, tagId, keyword, null));
    }

    public Result<ArticleDetailVO> detail(Long id) {
        return blogRemoteService.article(UserContext.toJson(), toId(id));
    }

    /** 全文搜索：GET query → PageQueryDTO（仅 keyword/page/size 有效） */
    public Result<PageVO<ArticleCardVO>> search(String keyword, long page, long size) {
        PageQueryDTO dto = new PageQueryDTO();
        dto.setKeyword(keyword);
        dto.setPage(page);
        dto.setSize(size);
        return blogRemoteService.search(dto);
    }

    // ==================== 互动 ====================

    /** 上报浏览：ip 已含在 X-User-Info 中，供下游"按访客按天去重"，登录用户优先按 userId 去重 */
    public Result<Boolean> view(Long id) {
        return blogRemoteService.view(UserContext.toJson(), toId(id));
    }

    public Result<Boolean> like(Long id) {
        return blogRemoteService.like(UserContext.toJson(), toId(id));
    }

    public Result<Boolean> unlike(Long id) {
        return blogRemoteService.unlike(UserContext.toJson(), toId(id));
    }

    public Result<Void> share(Long id, ShareDTO dto) {
        dto.setArticleId(id);
        return blogRemoteService.share(UserContext.toJson(), dto);
    }

    /** 上报观看时长：ip 已含在 X-User-Info 中，供下游"按访客+日期"聚合 */
    public Result<Void> watch(Long id, WatchDTO dto) {
        dto.setArticleId(id);
        return blogRemoteService.watch(UserContext.toJson(), dto);
    }

    // ==================== 评论 ====================

    /** 评论列表：GET query → CommentQueryDTO */
    public Result<PageVO<CommentVO>> comments(Long articleId, long page, long size) {
        CommentQueryDTO dto = new CommentQueryDTO();
        dto.setArticleId(articleId);
        dto.setPage(page);
        dto.setSize(size);
        return blogRemoteService.comments(dto);
    }

    public Result<CommentVO> createComment(CommentCreateDTO dto) {
        return blogRemoteService.createComment(UserContext.toJson(), dto);
    }

    public Result<Void> deleteComment(Long id) {
        return blogRemoteService.deleteComment(UserContext.toJson(), toId(id));
    }

    // ==================== 分类 / 标签 ====================

    public Result<List<CategoryVO>> categories() {
        return blogRemoteService.categories();
    }

    public Result<List<TagVO>> tags() {
        return blogRemoteService.tags();
    }

    public Result<String> uploadImage(MultipartFile file) {
        return blogRemoteService.uploadImage(UserContext.toJson(), file);
    }

    // ==================== 管理端：文章 ====================

    public Result<PageVO<ArticleCardVO>> adminPage(long page, long size, Long categoryId, Long tagId, String keyword) {
        return blogRemoteService.adminArticles(UserContext.toJson(),
                toQuery(page, size, categoryId, tagId, keyword, null));
    }

    public Result<ArticleDetailVO> createArticle(ArticleSaveDTO dto) {
        return blogRemoteService.createArticle(UserContext.toJson(), dto);
    }

    public Result<ArticleDetailVO> updateArticle(Long id, ArticleSaveDTO dto) {
        dto.setId(id);
        return blogRemoteService.updateArticle(UserContext.toJson(), dto);
    }

    public Result<Void> deleteArticle(Long id) {
        return blogRemoteService.deleteArticle(UserContext.toJson(), toId(id));
    }

    public Result<Void> updateArticleStatus(Long id, ArticleStatusDTO dto) {
        dto.setId(id);
        return blogRemoteService.updateArticleStatus(UserContext.toJson(), dto);
    }

    /** 单 ID → IdDTO（服务间 POST 不使用路径参数） */
    private IdDTO toId(Long id) {
        IdDTO dto = new IdDTO();
        dto.setId(id);
        return dto;
    }

    /** GET 查询参数 → 分页查询 DTO（空值字段不下发，下游按默认值处理） */
    private PageQueryDTO toQuery(long page, long size, Long categoryId, Long tagId, String keyword, Integer status) {
        PageQueryDTO dto = new PageQueryDTO();
        dto.setPage(page);
        dto.setSize(size);
        if (categoryId != null) {
            dto.setCategoryId(categoryId);
        }
        if (tagId != null) {
            dto.setTagId(tagId);
        }
        if (keyword != null && !keyword.isBlank()) {
            dto.setKeyword(keyword);
        }
        if (status != null) {
            dto.setStatus(status);
        }
        return dto;
    }
}
