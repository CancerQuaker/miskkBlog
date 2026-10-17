package cn.miskk.blog.gateway.handler;

import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.UserContext;
import cn.miskk.blog.common.vo.DashboardVO;
import cn.miskk.blog.common.vo.SystemInfoVO;
import cn.miskk.blog.gateway.service.BlogRemoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 管理端（仪表盘 / 系统监控）请求处理：
 * 用户上下文以单个 X-User-Info 请求头（LoginUser JSON）下发。
 */
@Component
@RequiredArgsConstructor
public class AdminHandler {

    private final BlogRemoteService blogRemoteService;

    public Result<DashboardVO> dashboard() {
        return blogRemoteService.dashboardSummary(UserContext.toJson());
    }

    public Result<SystemInfoVO> system() {
        return blogRemoteService.monitorSystem(UserContext.toJson());
    }
}
