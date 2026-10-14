package cn.miskk.blog.gateway.controller;

import cn.miskk.blog.common.api.PageVO;
import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.dto.ArticleSaveDTO;
import cn.miskk.blog.common.dto.ArticleStatusDTO;
import cn.miskk.blog.common.vo.ArticleCardVO;
import cn.miskk.blog.common.vo.ArticleDetailVO;
import cn.miskk.blog.gateway.handler.ArticleHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端文章入口（对前端保持原请求方式：GET 列表、POST 新增、PUT 编辑、DELETE 删除），
 * 请求/响应对象取自 common 共享 DTO/VO。
 */
@Tag(name = "网关-管理文章")
@RestController
@RequestMapping("/api/article/admin/articles")
@RequiredArgsConstructor
public class GatewayArticleAdminController {

    private final ArticleHandler articleHandler;

    @Operation(summary = "文章管理列表（含草稿/隐藏）")
    @GetMapping
    public Result<PageVO<ArticleCardVO>> page(@RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "10") long size,
                                              @RequestParam(required = false) Long categoryId,
                                              @RequestParam(required = false) Long tagId,
                                              @RequestParam(required = false) String keyword) {
        return articleHandler.adminPage(page, size, categoryId, tagId, keyword);
    }

    @Operation(summary = "新增文章")
    @PostMapping
    public Result<ArticleDetailVO> create(@RequestBody ArticleSaveDTO dto) {
        return articleHandler.createArticle(dto);
    }

    @Operation(summary = "编辑文章")
    @PutMapping("/{id}")
    public Result<ArticleDetailVO> update(@PathVariable Long id, @RequestBody ArticleSaveDTO dto) {
        return articleHandler.updateArticle(id, dto);
    }

    @Operation(summary = "删除文章")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return articleHandler.deleteArticle(id);
    }

    @Operation(summary = "修改文章状态（0 草稿 / 1 发布 / 2 隐藏）")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody ArticleStatusDTO dto) {
        return articleHandler.updateArticleStatus(id, dto);
    }
}
