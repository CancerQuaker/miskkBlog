package cn.miskk.blog.gateway.handler;

import cn.miskk.blog.common.api.PageVO;
import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.LoginUser;
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
 * 用户上下文（userId/role，浏览与观看另含客户端 ip）以参数形式显式下发，
 * 匿名请求 userId 为 null，Feign 会省略对应请求头。
 */
@Component
@RequiredArgsConstructor
public class ArticleHandler {

    private final BlogRemoteService blogRemoteService;

    // ==================== 文章浏览 ====================

    /** 文章分页列表：GET query → PageQueryDTO（登录管理员可见草稿/隐藏） */
    public Result<PageVO<ArticleCardVO>> page(long page, long size, Long categoryId, Long tagId, String keyword) {
        LoginUser user = UserContext.get();
        return blogRemoteService.articles(user.getUserId(), user.getRole(),
                toQuery(page, size, categoryId, tagId, keyword, null));
    }

    public Result<ArticleDetailVO> detail(Long id) {
        LoginUser user = UserContext.get();
        return blogRemoteService.article(user.getUserId(), user.getRole(), toId(id));
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

    /** 上报浏览：ip 供下游"按访客按天去重"，登录用户优先按 userId 去重 */
    public Result<Boolean> view(Long id) {
        LoginUser user = UserContext.get();
        return blogRemoteService.view(user.getUserId(), user.getRole(), user.getIp(), toId(id));
    }

    public Result<Boolean> like(Long id) {
        LoginUser user = UserContext.get();
        return blogRemoteService.like(user.getUserId(), user.getRole(), toId(id));
    }

    public Result<Boolean> unlike(Long id) {
        LoginUser user = UserContext.get();
        return blogRemoteService.unlike(user.getUserId(), user.getRole(), toId(id));
    }

    public Result<Void> share(Long id, ShareDTO dto) {
        LoginUser user = UserContext.get();
        dto.setArticleId(id);
        return blogRemoteService.share(user.getUserId(), user.getRole(), dto);
    }

    /** 上报观看时长：ip 供下游"按访客+日期"聚合 */
    public Result<Void> watch(Long id, WatchDTO dto) {
        LoginUser user = UserContext.get();
        dto.setArticleId(id);
        return blogRemoteService.watch(user.getUserId(), user.getRole(), user.getIp(), dto);
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
        LoginUser user = UserContext.get();
        return blogRemoteService.createComment(user.getUserId(), user.getRole(), dto);
    }

    public Result<Void> deleteComment(Long id) {
        LoginUser user = UserContext.get();
        return blogRemoteService.deleteComment(user.getUserId(), user.getRole(), toId(id));
    }

    // ==================== 分类 / 标签 ====================

    public Result<List<CategoryVO>> categories() {
        return blogRemoteService.categories();
    }

    public Result<List<TagVO>> tags() {
        return blogRemoteService.tags();
    }

    public Result<String> uploadImage(MultipartFile file) {
        LoginUser user = UserContext.get();
        return blogRemoteService.uploadImage(user.getUserId(), user.getRole(), file);
    }

    // ==================== 管理端：文章 ====================

    public Result<PageVO<ArticleCardVO>> adminPage(long page, long size, Long categoryId, Long tagId, String keyword) {
        LoginUser user = UserContext.get();
        return blogRemoteService.adminArticles(user.getUserId(), user.getRole(),
                toQuery(page, size, categoryId, tagId, keyword, null));
    }

    public Result<ArticleDetailVO> createArticle(ArticleSaveDTO dto) {
        LoginUser user = UserContext.get();
        return blogRemoteService.createArticle(user.getUserId(), user.getRole(), dto);
    }

    public Result<ArticleDetailVO> updateArticle(Long id, ArticleSaveDTO dto) {
        LoginUser user = UserContext.get();
        dto.setId(id);
        return blogRemoteService.updateArticle(user.getUserId(), user.getRole(), dto);
    }

    public Result<Void> deleteArticle(Long id) {
        LoginUser user = UserContext.get();
        return blogRemoteService.deleteArticle(user.getUserId(), user.getRole(), toId(id));
    }

    public Result<Void> updateArticleStatus(Long id, ArticleStatusDTO dto) {
        LoginUser user = UserContext.get();
        dto.setId(id);
        return blogRemoteService.updateArticleStatus(user.getUserId(), user.getRole(), dto);
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
