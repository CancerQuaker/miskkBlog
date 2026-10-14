package cn.miskk.blog.controller;

import cn.miskk.blog.common.dto.ArticleSaveDTO;
import cn.miskk.blog.common.dto.ArticleStatusDTO;
import cn.miskk.blog.common.dto.IdDTO;
import cn.miskk.blog.common.dto.PageQueryDTO;
import cn.miskk.blog.service.ArticleService;
import cn.miskk.blog.common.vo.ArticleCardVO;
import cn.miskk.blog.common.vo.ArticleDetailVO;
import cn.miskk.blog.common.api.PageVO;
import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.RequireAdmin;
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
 * 管理端文章接口（管理员），统一 POST，资源 ID 一律从请求体 DTO 获取。
 */
@Tag(name = "管理-文章")
@RestController
@RequestMapping("/admin/articles")
@RequiredArgsConstructor
public class AdminArticleController {

    private final ArticleService articleService;

    @Operation(summary = "文章管理列表（含草稿/隐藏）")
    @RequireAdmin
    @PostMapping("/list")
    public Result<PageVO<ArticleCardVO>> page(@RequestBody PageQueryDTO q) {
        return Result.ok(articleService.page(q.getPage(), q.getSize(),
                q.getCategoryId(), q.getTagId(), q.getKeyword(), false));
    }

    @Operation(summary = "新增文章")
    @RequireAdmin
    @PostMapping
    public Result<ArticleDetailVO> create(@Valid @RequestBody ArticleSaveDTO dto) {
        return Result.ok(articleService.save(dto, UserContext.getUserId(), null));
    }

    @Operation(summary = "编辑文章")
    @RequireAdmin
    @PostMapping("/update")
    public Result<ArticleDetailVO> update(@Valid @RequestBody ArticleSaveDTO dto) {
        return Result.ok(articleService.save(dto, UserContext.getUserId(), dto.getId()));
    }

    @Operation(summary = "删除文章")
    @RequireAdmin
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody IdDTO dto) {
        articleService.delete(dto.getId());
        return Result.ok();
    }

    @Operation(summary = "修改文章状态（0 草稿 / 1 发布 / 2 隐藏）")
    @RequireAdmin
    @PostMapping("/status")
    public Result<Void> updateStatus(@Valid @RequestBody ArticleStatusDTO dto) {
        articleService.updateStatus(dto.getId(), dto.getStatus());
        return Result.ok();
    }
}
