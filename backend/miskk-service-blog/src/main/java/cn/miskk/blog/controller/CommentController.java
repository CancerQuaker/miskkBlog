package cn.miskk.blog.controller;

import cn.miskk.blog.common.dto.CommentCreateDTO;
import cn.miskk.blog.common.dto.CommentQueryDTO;
import cn.miskk.blog.common.dto.IdDTO;
import cn.miskk.blog.service.CommentService;
import cn.miskk.blog.common.vo.CommentVO;
import cn.miskk.blog.common.api.PageVO;
import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.RequireLogin;
import cn.miskk.blog.common.auth.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论接口：游客可读，登录可评论，统一 POST。
 */
@Tag(name = "评论")
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "评论列表（树形，按时间倒序）")
    @PostMapping("/list")
    public Result<PageVO<CommentVO>> page(@Valid @RequestBody CommentQueryDTO q) {
        return Result.ok(commentService.pageByArticle(q.getArticleId(), q.getPage(), q.getSize()));
    }

    @Operation(summary = "发表评论/回复（需登录）")
    @RequireLogin
    @PostMapping
    public Result<CommentVO> create(@Valid @RequestBody CommentCreateDTO dto) {
        return Result.ok(commentService.create(dto.getArticleId(), UserContext.getUserId(),
                dto.getContent(), dto.getParentId(), dto.getReplyUserId()));
    }

    @Operation(summary = "删除自己的评论")
    @RequireLogin
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody IdDTO dto) {
        commentService.delete(dto.getId(), UserContext.get());
        return Result.ok();
    }
}
