package cn.miskk.blog.controller;

import cn.miskk.blog.common.api.PageVO;
import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.RequireAdmin;
import cn.miskk.blog.common.dto.FriendReviewDTO;
import cn.miskk.blog.common.dto.IdDTO;
import cn.miskk.blog.common.dto.PageQueryDTO;
import cn.miskk.blog.common.entity.FriendApply;
import cn.miskk.blog.common.entity.FriendLink;
import cn.miskk.blog.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 友链管理接口（审核 + 维护），统一 POST，资源 ID 一律从请求体 DTO 获取。
 */
@Tag(name = "管理-友链")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class FriendAdminController {

    private final FriendService friendService;

    @Operation(summary = "申请列表（按状态筛选）")
    @RequireAdmin
    @PostMapping("/applies")
    public Result<PageVO<FriendApply>> applies(@RequestBody PageQueryDTO q) {
        return Result.ok(friendService.pageApplies(q.getPage(), q.getSize(), q.getStatus()));
    }

    @Operation(summary = "审核友链申请")
    @RequireAdmin
    @PostMapping("/applies/review")
    public Result<FriendApply> review(@Valid @RequestBody FriendReviewDTO dto) {
        return Result.ok(friendService.review(dto.getApplyId(), dto));
    }

    @Operation(summary = "友链管理列表")
    @RequireAdmin
    @PostMapping("/links")
    public Result<PageVO<FriendLink>> links(@RequestBody PageQueryDTO q) {
        return Result.ok(friendService.pageLinks(q.getPage(), q.getSize()));
    }

    @Operation(summary = "编辑友链")
    @RequireAdmin
    @PostMapping("/links/update")
    public Result<FriendLink> update(@Valid @RequestBody FriendLink link) {
        return Result.ok(friendService.updateLink(link.getId(), link));
    }

    @Operation(summary = "删除友链")
    @RequireAdmin
    @PostMapping("/links/delete")
    public Result<Void> delete(@Valid @RequestBody IdDTO dto) {
        friendService.deleteLink(dto.getId());
        return Result.ok();
    }
}
