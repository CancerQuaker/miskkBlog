package cn.miskk.blog.controller;

import cn.miskk.blog.entity.Category;
import cn.miskk.blog.service.CategoryService;
import cn.miskk.blog.common.dto.IdDTO;
import cn.miskk.blog.common.vo.CategoryVO;
import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.RequireAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类接口：公开读取 + 管理员维护，统一 POST、不使用路径参数。
 */
@Tag(name = "分类")
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "分类列表（含已发布文章数）")
    @PostMapping("/list")
    public Result<List<CategoryVO>> list() {
        return Result.ok(categoryService.listWithCount());
    }

    @Operation(summary = "新增分类（管理员）")
    @RequireAdmin
    @PostMapping
    public Result<Category> create(@RequestBody Category category) {
        return Result.ok(categoryService.create(category));
    }

    @Operation(summary = "修改分类（管理员）")
    @RequireAdmin
    @PostMapping("/update")
    public Result<Category> update(@RequestBody Category category) {
        return Result.ok(categoryService.update(category));
    }

    @Operation(summary = "删除分类（管理员）")
    @RequireAdmin
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody IdDTO dto) {
        categoryService.delete(dto.getId());
        return Result.ok();
    }
}
