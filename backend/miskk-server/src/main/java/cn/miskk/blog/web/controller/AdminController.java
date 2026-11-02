package cn.miskk.blog.web.controller;

import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.RequireAdmin;
import cn.miskk.blog.common.vo.DashboardVO;
import cn.miskk.blog.common.vo.SystemInfoVO;
import cn.miskk.blog.web.handler.AdminHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端（仪表盘 / 系统监控）入口（管理员，GET），响应对象取自 common 共享 VO。
 */
@Tag(name = "管理-后台")
@RestController
@RequireAdmin
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminHandler adminHandler;

    @Operation(summary = "站点统计聚合")
    @GetMapping("/dashboard/summary")
    public Result<DashboardVO> dashboard() {
        return adminHandler.dashboard();
    }

    @Operation(summary = "系统资源信息")
    @GetMapping("/monitor/system")
    public Result<SystemInfoVO> system() {
        return adminHandler.system();
    }
}
