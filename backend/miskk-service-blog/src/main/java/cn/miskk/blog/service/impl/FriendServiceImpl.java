package cn.miskk.blog.service.impl;

import cn.miskk.blog.common.api.PageVO;
import cn.miskk.blog.common.api.PageVOConverter;
import cn.miskk.blog.common.dto.FriendStatsVO;
import cn.miskk.blog.common.exception.BizException;
import cn.miskk.blog.common.dto.FriendApplyDTO;
import cn.miskk.blog.common.dto.FriendReviewDTO;
import cn.miskk.blog.common.entity.FriendApply;
import cn.miskk.blog.common.entity.FriendLink;
import cn.miskk.blog.mapper.FriendApplyMapper;
import cn.miskk.blog.mapper.FriendLinkMapper;
import cn.miskk.blog.service.FriendService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 友链服务实现。
 * <p>
 * 本服务为双数据源（article 主库 + friend 库），友链业务统一路由到 friend 库；
 * @DS 切面优先级高于事务切面，保证 {@code @Transactional} 事务绑定 friend 连接。
 */
@Service
@DS("friend")
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final FriendLinkMapper linkMapper;
    private final FriendApplyMapper applyMapper;

    @Override
    public List<FriendLink> listApproved() {
        return linkMapper.selectList(Wrappers.<FriendLink>lambdaQuery()
                .eq(FriendLink::getStatus, 1)
                .orderByAsc(FriendLink::getSort)
                .orderByAsc(FriendLink::getId));
    }

    @Override
    public FriendStatsVO statsOverview() {
        FriendStatsVO vo = new FriendStatsVO();
        vo.setTotalLinks(linkMapper.selectCount(Wrappers.<FriendLink>lambdaQuery()
                .eq(FriendLink::getStatus, 1)));
        vo.setPendingApplies(applyMapper.selectCount(Wrappers.<FriendApply>lambdaQuery()
                .eq(FriendApply::getStatus, 0)));
        return vo;
    }

    @Override
    public FriendApply apply(FriendApplyDTO dto) {
        if (!dto.getUrl().startsWith("http://") && !dto.getUrl().startsWith("https://")) {
            throw new BizException("站点链接必须以 http(s):// 开头");
        }
        Long dup = applyMapper.selectCount(Wrappers.<FriendApply>lambdaQuery()
                .eq(FriendApply::getUrl, dto.getUrl())
                .eq(FriendApply::getStatus, 0));
        if (dup != null && dup > 0) {
            throw new BizException("该站点已在审核中，请耐心等待");
        }
        FriendApply apply = new FriendApply();
        apply.setName(dto.getName());
        apply.setUrl(dto.getUrl());
        apply.setAvatar(dto.getAvatar());
        apply.setDescription(dto.getDescription());
        apply.setContact(dto.getContact());
        apply.setReason(dto.getReason());
        apply.setStatus(0);
        applyMapper.insert(apply);
        return apply;
    }

    @Override
    public PageVO<FriendApply> pageApplies(long page, long size, Integer status) {
        Page<FriendApply> p = new Page<>(page, Math.min(size, 50));
        Page<FriendApply> result = applyMapper.selectPage(p, Wrappers.<FriendApply>lambdaQuery()
                .eq(status != null, FriendApply::getStatus, status)
                .orderByAsc(FriendApply::getStatus)
                .orderByDesc(FriendApply::getCreatedAt));
        return PageVOConverter.of(result);
    }

    @Override
    @Transactional
    public FriendApply review(Long applyId, FriendReviewDTO dto) {
        FriendApply apply = applyMapper.selectById(applyId);
        if (apply == null) {
            throw new BizException("申请不存在");
        }
        if (apply.getStatus() != 0) {
            throw new BizException("该申请已处理过");
        }
        apply.setStatus(Boolean.TRUE.equals(dto.getApproved()) ? 1 : 2);
        apply.setReviewNote(dto.getNote());
        apply.setReviewedAt(LocalDateTime.now());

        if (Boolean.TRUE.equals(dto.getApproved())) {
            // 同意：创建或更新友链
            FriendLink link = linkMapper.selectOne(Wrappers.<FriendLink>lambdaQuery()
                    .eq(FriendLink::getUrl, apply.getUrl()).last("LIMIT 1"));
            if (link == null) {
                link = new FriendLink();
            }
            link.setName(apply.getName());
            link.setUrl(apply.getUrl());
            link.setAvatar(StringUtils.hasText(apply.getAvatar()) ? apply.getAvatar() : null);
            link.setDescription(apply.getDescription());
            link.setStatus(1);
            link.setSort(99);
            if (link.getId() == null) {
                linkMapper.insert(link);
            } else {
                linkMapper.updateById(link);
            }
            apply.setLinkId(link.getId());
        }
        applyMapper.updateById(apply);
        return apply;
    }

    @Override
    public PageVO<FriendLink> pageLinks(long page, long size) {
        Page<FriendLink> p = new Page<>(page, Math.min(size, 50));
        Page<FriendLink> result = linkMapper.selectPage(p, Wrappers.<FriendLink>lambdaQuery()
                .orderByAsc(FriendLink::getStatus)
                .orderByAsc(FriendLink::getSort));
        return PageVOConverter.of(result);
    }

    @Override
    public FriendLink updateLink(Long id, FriendLink link) {
        FriendLink exists = linkMapper.selectById(id);
        if (exists == null) {
            throw new BizException("友链不存在");
        }
        link.setId(id);
        linkMapper.updateById(link);
        return linkMapper.selectById(id);
    }

    @Override
    public void deleteLink(Long id) {
        linkMapper.deleteById(id);
    }
}
