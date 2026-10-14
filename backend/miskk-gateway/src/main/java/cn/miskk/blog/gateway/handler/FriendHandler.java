package cn.miskk.blog.gateway.handler;

import cn.miskk.blog.common.api.PageVO;
import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.LoginUser;
import cn.miskk.blog.common.auth.UserContext;
import cn.miskk.blog.common.dto.FriendApplyDTO;
import cn.miskk.blog.common.dto.FriendReviewDTO;
import cn.miskk.blog.common.dto.IdDTO;
import cn.miskk.blog.common.dto.PageQueryDTO;
import cn.miskk.blog.common.entity.FriendApply;
import cn.miskk.blog.common.entity.FriendLink;
import cn.miskk.blog.common.vo.FriendNotesVO;
import cn.miskk.blog.gateway.service.BlogRemoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 友链请求处理（公开接口 + 管理端审核维护）：
 * GET query → PageQueryDTO；管理端接口的用户上下文以参数形式显式下发。
 */
@Component
@RequiredArgsConstructor
public class FriendHandler {

    private final BlogRemoteService blogRemoteService;

    public Result<List<FriendLink>> links() {
        return blogRemoteService.friendLinks();
    }

    public Result<FriendNotesVO> notes() {
        return blogRemoteService.friendNotes();
    }

    public Result<FriendApply> apply(FriendApplyDTO dto) {
        return blogRemoteService.applyFriend(dto);
    }

    /** 申请管理列表：GET query → PageQueryDTO（status 筛选可选） */
    public Result<PageVO<FriendApply>> adminApplies(long page, long size, Integer status) {
        LoginUser user = UserContext.get();
        PageQueryDTO dto = new PageQueryDTO();
        dto.setPage(page);
        dto.setSize(size);
        if (status != null) {
            dto.setStatus(status);
        }
        return blogRemoteService.adminApplies(user.getUserId(), user.getRole(), dto);
    }

    public Result<FriendApply> reviewApply(Long id, FriendReviewDTO dto) {
        LoginUser user = UserContext.get();
        dto.setApplyId(id);
        return blogRemoteService.reviewApply(user.getUserId(), user.getRole(), dto);
    }

    /** 友链管理列表：GET query → PageQueryDTO */
    public Result<PageVO<FriendLink>> adminLinks(long page, long size) {
        LoginUser user = UserContext.get();
        PageQueryDTO dto = new PageQueryDTO();
        dto.setPage(page);
        dto.setSize(size);
        return blogRemoteService.adminLinks(user.getUserId(), user.getRole(), dto);
    }

    public Result<FriendLink> updateLink(Long id, FriendLink link) {
        LoginUser user = UserContext.get();
        link.setId(id);
        return blogRemoteService.updateLink(user.getUserId(), user.getRole(), link);
    }

    public Result<Void> deleteLink(Long id) {
        LoginUser user = UserContext.get();
        IdDTO dto = new IdDTO();
        dto.setId(id);
        return blogRemoteService.deleteLink(user.getUserId(), user.getRole(), dto);
    }
}
