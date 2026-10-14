package cn.miskk.blog.common.dto;

import lombok.Data;

/**
 * 友链服务站点统计（供管理服务聚合）。
 */
@Data
public class FriendStatsVO {

    private Long totalLinks;
    private Long pendingApplies;
}
