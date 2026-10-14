package cn.miskk.blog.common.dto;

import lombok.Data;

/**
 * 用户服务站点统计（供管理服务聚合）。
 */
@Data
public class UserStatsVO {

    /** 用户总数 */
    private Long totalUsers;
    /** 本月新增用户 */
    private Long newUsersThisMonth;
}
