package cn.miskk.blog.common.vo;

import cn.miskk.blog.common.dto.ArticleStatsVO;
import cn.miskk.blog.common.dto.FriendStatsVO;
import cn.miskk.blog.common.dto.UserStatsVO;
import lombok.Data;

/**
 * 管理仪表盘聚合数据。
 */
@Data
public class DashboardVO {

    private UserStatsVO users;
    private ArticleStatsVO articles;
    private FriendStatsVO friends;
}
