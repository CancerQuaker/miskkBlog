package cn.miskk.blog.gateway.service;

import cn.miskk.blog.common.api.PageVO;
import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.constant.SecurityConstants;
import cn.miskk.blog.common.dto.ArticleSaveDTO;
import cn.miskk.blog.common.dto.ArticleStatusDTO;
import cn.miskk.blog.common.dto.CommentCreateDTO;
import cn.miskk.blog.common.dto.CommentQueryDTO;
import cn.miskk.blog.common.dto.FriendApplyDTO;
import cn.miskk.blog.common.dto.FriendReviewDTO;
import cn.miskk.blog.common.dto.IdDTO;
import cn.miskk.blog.common.dto.PageQueryDTO;
import cn.miskk.blog.common.dto.ShareDTO;
import cn.miskk.blog.common.dto.WatchDTO;
import cn.miskk.blog.common.entity.FriendApply;
import cn.miskk.blog.common.entity.FriendLink;
import cn.miskk.blog.common.vo.ArticleCardVO;
import cn.miskk.blog.common.vo.ArticleDetailVO;
import cn.miskk.blog.common.vo.CategoryVO;
import cn.miskk.blog.common.vo.CommentVO;
import cn.miskk.blog.common.vo.DashboardVO;
import cn.miskk.blog.common.vo.FriendNotesVO;
import cn.miskk.blog.common.vo.SystemInfoVO;
import cn.miskk.blog.common.vo.TagVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 博客服务远程接口（Feign，统一 POST、不使用路径参数：资源 ID 一律放请求体 DTO）。
 * <p>
 * 覆盖文章/互动/评论/分类/标签/上传、友链公开与管理、仪表盘统计与系统监控；
 * 请求/响应对象统一取自 common 的 DTO/VO。
 * <p>
 * 用户上下文由网关解析 JWT 后以单个请求头 {@code X-User-Info} 下发
 * （{@link cn.miskk.blog.common.auth.LoginUser LoginUser} 序列化的 JSON，取值 {@code UserContext.toJson()}）：
 * userId 为 null 表示匿名；ip 为网关解析的客户端真实 IP，
 * 浏览/观看去重接口按"登录用户优先、否则 IP"生成访客标识，直接从还原后的上下文取。
 */
@FeignClient(name = "miskk-blog", contextId = "gatewayBlogClient")
public interface BlogRemoteService {

    // ==================== 文章浏览与互动 ====================

    @PostMapping("/articles")
    Result<PageVO<ArticleCardVO>> articles(@RequestHeader(SecurityConstants.HEADER_USER_INFO) String userInfo,
                                           @RequestBody PageQueryDTO dto);

    @PostMapping("/articles/detail")
    Result<ArticleDetailVO> article(@RequestHeader(SecurityConstants.HEADER_USER_INFO) String userInfo,
                                    @RequestBody IdDTO dto);

    @PostMapping("/articles/search")
    Result<PageVO<ArticleCardVO>> search(@RequestBody PageQueryDTO dto);

    @PostMapping("/articles/view")
    Result<Boolean> view(@RequestHeader(SecurityConstants.HEADER_USER_INFO) String userInfo,
                         @RequestBody IdDTO dto);

    @PostMapping("/articles/like")
    Result<Boolean> like(@RequestHeader(SecurityConstants.HEADER_USER_INFO) String userInfo,
                         @RequestBody IdDTO dto);

    @PostMapping("/articles/unlike")
    Result<Boolean> unlike(@RequestHeader(SecurityConstants.HEADER_USER_INFO) String userInfo,
                           @RequestBody IdDTO dto);

    @PostMapping("/articles/share")
    Result<Void> share(@RequestHeader(SecurityConstants.HEADER_USER_INFO) String userInfo,
                       @RequestBody ShareDTO dto);

    @PostMapping("/articles/watch")
    Result<Void> watch(@RequestHeader(SecurityConstants.HEADER_USER_INFO) String userInfo,
                       @RequestBody WatchDTO dto);

    // ==================== 评论 ====================

    @PostMapping("/comments/list")
    Result<PageVO<CommentVO>> comments(@RequestBody CommentQueryDTO dto);

    @PostMapping("/comments")
    Result<CommentVO> createComment(@RequestHeader(SecurityConstants.HEADER_USER_INFO) String userInfo,
                                    @RequestBody CommentCreateDTO dto);

    @PostMapping("/comments/delete")
    Result<Void> deleteComment(@RequestHeader(SecurityConstants.HEADER_USER_INFO) String userInfo,
                               @RequestBody IdDTO dto);

    // ==================== 分类 / 标签 ====================

    @PostMapping("/categories/list")
    Result<List<CategoryVO>> categories();

    @PostMapping("/tags/list")
    Result<List<TagVO>> tags();

    // ==================== 图片上传 ====================

    /** 文章配图上传（需登录），返回可访问的图片 URL */
    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<String> uploadImage(@RequestHeader(SecurityConstants.HEADER_USER_INFO) String userInfo,
                               @RequestPart("file") MultipartFile file);

    // ==================== 管理端：文章 ====================

    @PostMapping("/admin/articles/list")
    Result<PageVO<ArticleCardVO>> adminArticles(@RequestHeader(SecurityConstants.HEADER_USER_INFO) String userInfo,
                                                @RequestBody PageQueryDTO dto);

    @PostMapping("/admin/articles")
    Result<ArticleDetailVO> createArticle(@RequestHeader(SecurityConstants.HEADER_USER_INFO) String userInfo,
                                          @RequestBody ArticleSaveDTO dto);

    @PostMapping("/admin/articles/update")
    Result<ArticleDetailVO> updateArticle(@RequestHeader(SecurityConstants.HEADER_USER_INFO) String userInfo,
                                          @RequestBody ArticleSaveDTO dto);

    @PostMapping("/admin/articles/delete")
    Result<Void> deleteArticle(@RequestHeader(SecurityConstants.HEADER_USER_INFO) String userInfo,
                               @RequestBody IdDTO dto);

    @PostMapping("/admin/articles/status")
    Result<Void> updateArticleStatus(@RequestHeader(SecurityConstants.HEADER_USER_INFO) String userInfo,
                                     @RequestBody ArticleStatusDTO dto);

    // ==================== 友链（公开 + 管理） ====================

    @PostMapping("/links")
    Result<List<FriendLink>> friendLinks();

    @PostMapping("/apply/notes")
    Result<FriendNotesVO> friendNotes();

    @PostMapping("/apply")
    Result<FriendApply> applyFriend(@RequestBody FriendApplyDTO dto);

    @PostMapping("/admin/applies")
    Result<PageVO<FriendApply>> adminApplies(@RequestHeader(SecurityConstants.HEADER_USER_INFO) String userInfo,
                                             @RequestBody PageQueryDTO dto);

    @PostMapping("/admin/applies/review")
    Result<FriendApply> reviewApply(@RequestHeader(SecurityConstants.HEADER_USER_INFO) String userInfo,
                                    @RequestBody FriendReviewDTO dto);

    @PostMapping("/admin/links")
    Result<PageVO<FriendLink>> adminLinks(@RequestHeader(SecurityConstants.HEADER_USER_INFO) String userInfo,
                                          @RequestBody PageQueryDTO dto);

    @PostMapping("/admin/links/update")
    Result<FriendLink> updateLink(@RequestHeader(SecurityConstants.HEADER_USER_INFO) String userInfo,
                                  @RequestBody FriendLink link);

    @PostMapping("/admin/links/delete")
    Result<Void> deleteLink(@RequestHeader(SecurityConstants.HEADER_USER_INFO) String userInfo,
                            @RequestBody IdDTO dto);

    // ==================== 管理端：仪表盘 / 监控 ====================

    @PostMapping("/dashboard/summary")
    Result<DashboardVO> dashboardSummary(@RequestHeader(SecurityConstants.HEADER_USER_INFO) String userInfo);

    @PostMapping("/monitor/system")
    Result<SystemInfoVO> monitorSystem(@RequestHeader(SecurityConstants.HEADER_USER_INFO) String userInfo);
}
