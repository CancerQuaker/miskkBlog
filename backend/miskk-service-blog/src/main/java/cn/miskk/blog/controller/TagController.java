package cn.miskk.blog.controller;

import cn.miskk.blog.entity.Tag;
import cn.miskk.blog.service.TagService;
import cn.miskk.blog.common.dto.IdDTO;
import cn.miskk.blog.common.vo.TagVO;
import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.RequireAdmin;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 标签接口：公开读取 + 管理员维护，统一 POST、不使用路径参数。
 */
@io.swagger.v3.oas.annotations.tags.Tag(name = "标签")
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @Operation(summary = "标签列表（含文章数）")
    @PostMapping("/list")
    public Result<List<TagVO>> list() {
        return Result.ok(tagService.listWithCount());
    }

    @Operation(summary = "新增标签（管理员）")
    @RequireAdmin
    @PostMapping
    public Result<Tag> create(@RequestBody Tag tag) {
        return Result.ok(tagService.create(tag));
    }

    @Operation(summary = "修改标签（管理员）")
    @RequireAdmin
    @PostMapping("/update")
    public Result<Tag> update(@RequestBody Tag tag) {
        return Result.ok(tagService.update(tag));
    }

    @Operation(summary = "删除标签（管理员）")
    @RequireAdmin
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody IdDTO dto) {
        tagService.delete(dto.getId());
        return Result.ok();
    }
}
