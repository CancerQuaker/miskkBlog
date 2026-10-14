package cn.miskk.blog.controller;

import cn.miskk.blog.common.vo.DashboardVO;
import cn.miskk.blog.service.ArticleService;
import cn.miskk.blog.service.UserRemoteService;
import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.RequireAdmin;
import cn.miskk.blog.common.dto.ArticleStatsVO;
import cn.miskk.blog.common.dto.FriendStatsVO;
import cn.miskk.blog.common.dto.UserStatsVO;
import cn.miskk.blog.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理仪表盘：文章/友链统计为本服务进程内直调，用户统计走 Feign。
 */
@Slf4j
@Tag(name = "管理-仪表盘")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserRemoteService userRemoteService;
    private final ArticleService articleService;
    private final FriendService friendService;

    /** 服务间内部调用令牌，Feign 调用时以参数形式显式传递 */
    @Value("${miskk.internal-token:miskk-internal-token-dev}")
    private String internalToken;

    @Operation(summary = "站点统计聚合")
    @RequireAdmin
    @PostMapping("/summary")
    public Result<DashboardVO> summary() {
        DashboardVO vo = new DashboardVO();
        try {
            Result<UserStatsVO> users = userRemoteService.userStats(internalToken);
            vo.setUsers(users == null ? null : users.getData());
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
}
