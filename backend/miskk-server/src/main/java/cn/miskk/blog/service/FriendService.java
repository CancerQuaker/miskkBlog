package cn.miskk.blog.service;

import cn.miskk.blog.common.api.PageVO;
import cn.miskk.blog.common.dto.FriendStatsVO;
import cn.miskk.blog.common.dto.FriendApplyDTO;
import cn.miskk.blog.common.dto.FriendReviewDTO;
import cn.miskk.blog.common.entity.FriendApply;
import cn.miskk.blog.common.entity.FriendLink;

import java.util.List;

/**
 * 友链服务。
 */
public interface FriendService {

    List<FriendLink> listApproved();

    FriendStatsVO statsOverview();

    FriendApply apply(FriendApplyDTO dto);

    PageVO<FriendApply> pageApplies(long page, long size, Integer status);

    FriendApply review(Long applyId, FriendReviewDTO dto);

    PageVO<FriendLink> pageLinks(long page, long size);

    FriendLink updateLink(Long id, FriendLink link);

    void deleteLink(Long id);
}
