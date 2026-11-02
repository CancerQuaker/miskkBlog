package cn.miskk.blog.web.handler;

import cn.miskk.blog.common.api.PageVO;
import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.dto.FriendApplyDTO;
import cn.miskk.blog.common.dto.FriendReviewDTO;
import cn.miskk.blog.common.entity.FriendApply;
import cn.miskk.blog.common.entity.FriendLink;
import cn.miskk.blog.common.vo.FriendNotesVO;
import cn.miskk.blog.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 友链请求处理（公开接口 + 管理端审核维护）。
 */
@Component
@RequiredArgsConstructor
public class FriendHandler {

    private final FriendService friendService;

    public Result<List<FriendLink>> links() {
        return Result.ok(friendService.listApproved());
    }

    /** 友链申请须知（必填项 / 选填项 / 规则） */
    public Result<FriendNotesVO> notes() {
        FriendNotesVO notes = new FriendNotesVO();
        notes.setRequired(List.of("站点名称", "站点链接（http/https）", "联系方式"));
        notes.setOptional(List.of("站点头像链接", "站点简介（100 字内）", "申请说明"));
        notes.setRules(List.of(
                "站点需可正常访问且内容健康，不含违法违规内容",
                "请先将本站添加至你的友链，通过率更高哦",
                "本站信息：名称 迷失客客の博客，链接 https://blog.miskk.cn，头像 https://blog.miskk.cn/logo.png",
                "审核结果将通过联系方式通知，一般在 3 天内完成"
        ));
        return Result.ok(notes);
    }

    public Result<FriendApply> apply(FriendApplyDTO dto) {
        return Result.ok(friendService.apply(dto));
    }

    /** 申请管理列表（status 筛选可选） */
    public Result<PageVO<FriendApply>> adminApplies(long page, long size, Integer status) {
        return Result.ok(friendService.pageApplies(page, size, status));
    }

    public Result<FriendApply> reviewApply(Long id, FriendReviewDTO dto) {
        dto.setApplyId(id);
        return Result.ok(friendService.review(id, dto));
    }

    public Result<PageVO<FriendLink>> adminLinks(long page, long size) {
        return Result.ok(friendService.pageLinks(page, size));
    }

    public Result<FriendLink> updateLink(Long id, FriendLink link) {
        link.setId(id);
        return Result.ok(friendService.updateLink(id, link));
    }

    public Result<Void> deleteLink(Long id) {
        friendService.deleteLink(id);
        return Result.ok();
    }
}
