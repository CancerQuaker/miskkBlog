package cn.miskk.blog.user.controller;

import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.dto.IdDTO;
import cn.miskk.blog.common.dto.UserBatchDTO;
import cn.miskk.blog.common.dto.UserCardVO;
import cn.miskk.blog.common.dto.UserStatsVO;
import cn.miskk.blog.user.service.UserService;
import cn.miskk.blog.common.vo.UserVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 服务间内部接口（X-Internal-Token 由 InternalTokenInterceptor 统一校验），
 * 统一 POST、不使用路径参数。
 */
@Tag(name = "内部接口")
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserService userService;

    @PostMapping("/users/batch")
    public Result<Map<Long, UserCardVO>> cards(@RequestBody UserBatchDTO dto) {
        return Result.ok(userService.getUserCards(dto.getIds()));
    }

    @PostMapping("/users")
    public Result<UserVO> user(@Valid @RequestBody IdDTO dto) {
        return Result.ok(userService.getUserPublic(dto.getId()));
    }

    @PostMapping("/stats/overview")
    public Result<UserStatsVO> stats() {
        return Result.ok(userService.statsOverview());
    }
}
