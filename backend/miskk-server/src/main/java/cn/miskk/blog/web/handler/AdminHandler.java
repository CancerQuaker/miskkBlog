package cn.miskk.blog.web.handler;

import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.dto.ArticleStatsVO;
import cn.miskk.blog.common.dto.FriendStatsVO;
import cn.miskk.blog.common.dto.UserStatsVO;
import cn.miskk.blog.common.vo.DashboardVO;
import cn.miskk.blog.common.vo.SystemInfoVO;
import cn.miskk.blog.service.ArticleService;
import cn.miskk.blog.service.FriendService;
import cn.miskk.blog.service.SystemMonitorService;
import cn.miskk.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 管理端（仪表盘 / 系统监控）请求处理。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminHandler {

    private final UserService userService;
    private final ArticleService articleService;
    private final FriendService friendService;
    private final SystemMonitorService systemMonitorService;

    /** 站点统计聚合：用户/文章/友链三块各自独立降级，任一失败不影响其余 */
    public Result<DashboardVO> dashboard() {
        DashboardVO vo = new DashboardVO();
        try {
            UserStatsVO users = userService.statsOverview();
            vo.setUsers(users);
        } catch (Exception e) {
            log.warn("用户统计获取失败: {}", e.getMessage());
        }
        try {
            ArticleStatsVO articles = articleService.statsOverview();
            vo.setArticles(articles);
        } catch (Exception e) {
            log.warn("文章统计获取失败: {}", e.getMessage());
        }
        try {
            FriendStatsVO friends = friendService.statsOverview();
            vo.setFriends(friends);
        } catch (Exception e) {
            log.warn("友链统计获取失败: {}", e.getMessage());
        }
        return Result.ok(vo);
    }

    public Result<SystemInfoVO> system() {
        return Result.ok(systemMonitorService.collect());
    }
}
