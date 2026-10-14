package cn.miskk.blog.controller;

import cn.miskk.blog.common.dto.FriendApplyDTO;
import cn.miskk.blog.common.entity.FriendApply;
import cn.miskk.blog.common.entity.FriendLink;
import cn.miskk.blog.common.vo.FriendNotesVO;
import cn.miskk.blog.service.FriendService;
import cn.miskk.blog.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 友链公开接口：展示 + 申请，统一 POST。
 */
@Tag(name = "友链")
@RestController
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "已通过的友链列表")
    @PostMapping("/links")
    public Result<List<FriendLink>> links() {
        return Result.ok(friendService.listApproved());
    }

    @Operation(summary = "友链申请注意事项")
    @PostMapping("/apply/notes")
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

    @Operation(summary = "提交友链申请")
    @PostMapping("/apply")
    public Result<FriendApply> apply(@Valid @RequestBody FriendApplyDTO dto) {
        return Result.ok(friendService.apply(dto));
    }
}
