package cn.miskk.blog.user.controller;

import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.RequireLogin;
import cn.miskk.blog.common.auth.UserContext;
import cn.miskk.blog.common.dto.IdDTO;
import cn.miskk.blog.common.dto.UpdatePasswordDTO;
import cn.miskk.blog.common.dto.UpdateProfileDTO;
import cn.miskk.blog.user.service.UserService;
import cn.miskk.blog.common.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户资料接口，统一 POST。
 */
@Tag(name = "用户资料")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "我的资料")
    @RequireLogin
    @PostMapping("/user/me")
    public Result<UserVO> me() {
        return Result.ok(userService.me(UserContext.getUserId()));
    }

    @Operation(summary = "修改我的资料")
    @RequireLogin
    @PostMapping("/user/me/update")
    public Result<UserVO> updateMe(@Valid @RequestBody UpdateProfileDTO dto) {
        return Result.ok(userService.updateProfile(UserContext.getUserId(), dto));
    }

    @Operation(summary = "修改密码")
    @RequireLogin
    @PostMapping("/user/me/password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordDTO dto) {
        userService.updatePassword(UserContext.getUserId(), dto);
        return Result.ok();
    }

    @Operation(summary = "用户公开主页信息")
    @PostMapping("/users")
    public Result<UserVO> user(@Valid @RequestBody IdDTO dto) {
        return Result.ok(userService.getUserPublic(dto.getId()));
    }
}
