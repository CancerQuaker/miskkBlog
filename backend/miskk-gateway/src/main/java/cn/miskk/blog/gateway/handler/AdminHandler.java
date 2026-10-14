package cn.miskk.blog.gateway.handler;

import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.LoginUser;
import cn.miskk.blog.common.auth.UserContext;
import cn.miskk.blog.common.vo.DashboardVO;
import cn.miskk.blog.common.vo.SystemInfoVO;
import cn.miskk.blog.gateway.service.BlogRemoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 管理端（仪表盘 / 系统监控）请求处理：用户上下文以参数形式显式下发。
 */
@Component
@RequiredArgsConstructor
public class AdminHandler {

    private final BlogRemoteService blogRemoteService;

    public Result<DashboardVO> dashboard() {
        LoginUser user = UserContext.get();
        return blogRemoteService.dashboardSummary(user.getUserId(), user.getRole());
    }

    public Result<SystemInfoVO> system() {
        LoginUser user = UserContext.get();
        return blogRemoteService.monitorSystem(user.getUserId(), user.getRole());
    }
}
