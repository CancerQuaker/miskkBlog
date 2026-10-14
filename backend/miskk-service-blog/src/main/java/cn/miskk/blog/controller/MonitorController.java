package cn.miskk.blog.controller;

import cn.miskk.blog.service.SystemMonitorService;
import cn.miskk.blog.common.vo.SystemInfoVO;
import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.RequireAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统监控：CPU / 内存 / 磁盘 / JVM。
 */
@Tag(name = "管理-系统监控")
@RestController
@RequestMapping("/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final SystemMonitorService monitorService;

    @Operation(summary = "系统资源信息")
    @RequireAdmin
    @PostMapping("/system")
    public Result<SystemInfoVO> system() {
        return Result.ok(monitorService.collect());
    }
}
