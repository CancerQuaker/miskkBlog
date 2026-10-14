package cn.miskk.blog.gateway.controller;

import cn.miskk.blog.common.api.PageVO;
import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.dto.FriendApplyDTO;
import cn.miskk.blog.common.dto.FriendReviewDTO;
import cn.miskk.blog.common.entity.FriendApply;
import cn.miskk.blog.common.entity.FriendLink;
import cn.miskk.blog.common.vo.FriendNotesVO;
import cn.miskk.blog.gateway.handler.FriendHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 友链入口（公开 + 管理端，对前端保持原请求方式），请求/响应对象取自 common 共享 DTO/VO/实体。
 */
@Tag(name = "网关-友链")
@RestController
@RequiredArgsConstructor
public class GatewayFriendController {

    private final FriendHandler friendHandler;

    // ==================== 公开 ====================

    @Operation(summary = "已通过的友链列表")
    @GetMapping("/api/friend/links")
    public Result<List<FriendLink>> links() {
        return friendHandler.links();
    }

    @Operation(summary = "友链申请注意事项")
    @GetMapping("/api/friend/apply/notes")
    public Result<FriendNotesVO> notes() {
        return friendHandler.notes();
    }

    @Operation(summary = "提交友链申请")
    @PostMapping("/api/friend/apply")
    public Result<FriendApply> apply(@RequestBody FriendApplyDTO dto) {
        return friendHandler.apply(dto);
    }

    // ==================== 管理端 ====================

    @Operation(summary = "申请列表（按状态筛选）")
    @GetMapping("/api/friend/admin/applies")
    public Result<PageVO<FriendApply>> applies(@RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "10") long size,
                                               @RequestParam(required = false) Integer status) {
        return friendHandler.adminApplies(page, size, status);
    }

    @Operation(summary = "审核友链申请")
    @PostMapping("/api/friend/admin/applies/{id}/review")
    public Result<FriendApply> review(@PathVariable Long id, @RequestBody FriendReviewDTO dto) {
        return friendHandler.reviewApply(id, dto);
    }

    @Operation(summary = "友链管理列表")
    @GetMapping("/api/friend/admin/links")
    public Result<PageVO<FriendLink>> adminLinks(@RequestParam(defaultValue = "1") long page,
                                                 @RequestParam(defaultValue = "20") long size) {
        return friendHandler.adminLinks(page, size);
    }

    @Operation(summary = "编辑友链")
    @PutMapping("/api/friend/admin/links/{id}")
    public Result<FriendLink> updateLink(@PathVariable Long id, @RequestBody FriendLink link) {
        return friendHandler.updateLink(id, link);
    }

    @Operation(summary = "删除友链")
    @DeleteMapping("/api/friend/admin/links/{id}")
    public Result<Void> removeLink(@PathVariable Long id) {
        return friendHandler.deleteLink(id);
    }
}
