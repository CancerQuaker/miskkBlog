package cn.miskk.blog.controller;

import cn.miskk.blog.common.dto.IdDTO;
import cn.miskk.blog.common.dto.PageQueryDTO;
import cn.miskk.blog.service.ArticleService;
import cn.miskk.blog.common.vo.ArticleCardVO;
import cn.miskk.blog.common.vo.ArticleDetailVO;
import cn.miskk.blog.common.api.PageVO;
import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.LoginUser;
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
 * 文章浏览接口（公开），统一 POST。
 */
@Tag(name = "文章")
@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "文章分页列表（按发布时间倒序，置顶优先）")
    @PostMapping
    public Result<PageVO<ArticleCardVO>> page(@RequestBody PageQueryDTO q) {
        LoginUser login = UserContext.get();
        boolean publishedOnly = login == null || !login.isAdmin();
        return Result.ok(articleService.page(q.getPage(), q.getSize(),
                q.getCategoryId(), q.getTagId(), q.getKeyword(), publishedOnly));
    }

    @Operation(summary = "文章详情")
    @PostMapping("/detail")
    public Result<ArticleDetailVO> detail(@Valid @RequestBody IdDTO dto) {
        return Result.ok(articleService.detail(dto.getId(), UserContext.get()));
    }

    @Operation(summary = "全文搜索")
    @PostMapping("/search")
    public Result<PageVO<ArticleCardVO>> search(@RequestBody PageQueryDTO q) {
        return Result.ok(articleService.search(q.getKeyword(), q.getPage(), q.getSize()));
    }
}
