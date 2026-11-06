package cn.miskk.blog.web.handler;

import cn.miskk.blog.common.api.PageVO;
import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.LoginUser;
import cn.miskk.blog.common.auth.UserContext;
import cn.miskk.blog.common.dto.ArticleSaveDTO;
import cn.miskk.blog.common.dto.ArticleStatusDTO;
import cn.miskk.blog.common.dto.CommentCreateDTO;
import cn.miskk.blog.common.dto.ShareDTO;
import cn.miskk.blog.common.util.UploadUtil;
import cn.miskk.blog.common.vo.ArticleCardVO;
import cn.miskk.blog.common.vo.ArticleDetailVO;
import cn.miskk.blog.common.vo.CategoryVO;
import cn.miskk.blog.common.vo.CommentVO;
import cn.miskk.blog.common.vo.TagVO;
import cn.miskk.blog.service.ArticleService;
import cn.miskk.blog.service.CategoryService;
import cn.miskk.blog.service.CommentService;
import cn.miskk.blog.service.InteractionService;
import cn.miskk.blog.service.LikeService;
import cn.miskk.blog.service.TagService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

/**
 * 文章请求处理（浏览/互动/评论/分类/标签/上传/管理端）：
 * 当前用户取自 AuthInterceptor 填充的 UserContext（匿名请求 userId 为 null）。
 */
@Component
@RequiredArgsConstructor
public class ArticleHandler {

    private final ArticleService articleService;
    private final LikeService likeService;
    private final InteractionService interactionService;
    private final CommentService commentService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final HttpServletRequest request;

    @Value("${miskk.upload.dir:${user.home}/.miskk/uploads}")
    private String uploadDir;

    /** 文章配图的访问前缀，与 WebConfig 中的静态资源映射对应 */
    @Value("${miskk.upload.article-url-prefix:/api/static/article}")
    private String urlPrefix;

    // ==================== 文章浏览 ====================

    /** 文章分页列表（登录管理员可见草稿/隐藏，其余仅已发布） */
    public Result<PageVO<ArticleCardVO>> page(long page, long size, Long categoryId, Long tagId, String keyword) {
        LoginUser login = UserContext.get();
        boolean publishedOnly = login == null || !login.isAdmin();
        return Result.ok(articleService.page(page, size, categoryId, tagId, blank(keyword), publishedOnly));
    }

    public Result<ArticleDetailVO> detail(Long id) {
        return Result.ok(articleService.detail(id, UserContext.get()));
    }

    /** 全文搜索（仅 keyword/page/size 有效） */
    public Result<PageVO<ArticleCardVO>> search(String keyword, long page, long size) {
        return Result.ok(articleService.search(keyword, page, size));
    }

    // ==================== 互动 ====================

    /** 上报浏览：登录用户按 userId 去重，匿名按客户端 ip 哈希去重（同一访客每日仅计一次） */
    public Result<Boolean> view(Long id) {
        return Result.ok(articleService.recordView(id, visitorIdentity()));
    }

    public Result<Boolean> like(Long id) {
        return Result.ok(likeService.like(id, UserContext.getUserId()));
    }

    public Result<Boolean> unlike(Long id) {
        return Result.ok(likeService.unlike(id, UserContext.getUserId()));
    }

    public Result<Void> share(Long id, ShareDTO dto) {
        interactionService.share(id, dto.getChannel(), UserContext.getUserId());
        return Result.ok();
    }

    // ==================== 评论 ====================

    public Result<PageVO<CommentVO>> comments(Long articleId, long page, long size) {
        return Result.ok(commentService.pageByArticle(articleId, page, size));
    }

    public Result<CommentVO> createComment(CommentCreateDTO dto) {
        return Result.ok(commentService.create(dto.getArticleId(), UserContext.getUserId(),
                dto.getContent(), dto.getParentId(), dto.getReplyUserId()));
    }

    public Result<Void> deleteComment(Long id) {
        commentService.delete(id, UserContext.get());
        return Result.ok();
    }

    // ==================== 分类 / 标签 / 上传 ====================

    public Result<List<CategoryVO>> categories() {
        return Result.ok(categoryService.listWithCount());
    }

    public Result<List<TagVO>> tags() {
        return Result.ok(tagService.listWithCount());
    }

    /** 上传图片（文章封面等），返回可直接访问的 URL */
    public Result<String> uploadImage(MultipartFile file) {
        String relative = UploadUtil.saveImage(file, uploadDir, "article");
        return Result.ok(urlPrefix + relative);
    }

    // ==================== 管理端：文章 ====================

    /** 管理列表（含草稿/隐藏） */
    public Result<PageVO<ArticleCardVO>> adminPage(long page, long size, Long categoryId, Long tagId, String keyword) {
        return Result.ok(articleService.page(page, size, categoryId, tagId, blank(keyword), false));
    }

    public Result<ArticleDetailVO> createArticle(ArticleSaveDTO dto) {
        return Result.ok(articleService.save(dto, UserContext.getUserId(), null));
    }

    public Result<ArticleDetailVO> updateArticle(Long id, ArticleSaveDTO dto) {
        dto.setId(id);
        return Result.ok(articleService.save(dto, UserContext.getUserId(), id));
    }

    public Result<Void> deleteArticle(Long id) {
        articleService.delete(id);
        return Result.ok();
    }

    public Result<Void> updateArticleStatus(Long id, ArticleStatusDTO dto) {
        articleService.updateStatus(id, dto.getStatus());
        return Result.ok();
    }

    /** 访客标识：登录用户优先按 userId；匿名用鉴权阶段解析的客户端 ip 哈希，上下文缺失时退回直连地址 */
    private String visitorIdentity() {
        LoginUser user = UserContext.get();
        if (user != null && user.getUserId() != null) {
            return "u:" + user.getUserId();
        }
        String ip = user == null || !StringUtils.hasText(user.getIp())
                ? request.getRemoteAddr() : user.getIp();
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((ip + "|miskk-salt").getBytes(StandardCharsets.UTF_8));
            return "ip:" + HexFormat.of().formatHex(digest, 0, 8);
        } catch (Exception e) {
            return "ip:" + ip;
        }
    }

    /** 查询关键词为纯空白时按未传入处理 */
    private String blank(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword;
    }
}
