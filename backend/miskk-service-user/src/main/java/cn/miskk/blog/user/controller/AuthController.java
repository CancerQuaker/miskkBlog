package cn.miskk.blog.user.controller;

import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.LoginUser;
import cn.miskk.blog.common.auth.RequireLogin;
import cn.miskk.blog.common.auth.UserContext;
import cn.miskk.blog.common.dto.EmailCodeDTO;
import cn.miskk.blog.common.dto.GithubLoginDTO;
import cn.miskk.blog.common.dto.LoginDTO;
import cn.miskk.blog.common.dto.RegisterDTO;
import cn.miskk.blog.user.service.UserService;
import cn.miskk.blog.common.vo.LoginVO;
import cn.miskk.blog.common.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：注册、登录（邮箱/GitHub/QQ/微信）、当前用户，统一 POST。
 */
@Tag(name = "认证")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "发送邮箱验证码")
    @PostMapping("/code")
    public Result<String> sendCode(@Valid @RequestBody EmailCodeDTO dto) {
        return Result.ok(userService.sendEmailCode(dto.getEmail()));
    }

    @Operation(summary = "邮箱验证码注册")
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.ok(userService.register(dto));
    }

    @Operation(summary = "账号密码登录（邮箱或用户名）")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(userService.login(dto));
    }

    @Operation(summary = "GitHub OAuth 登录")
    @PostMapping("/github")
    public Result<LoginVO> github(@Valid @RequestBody GithubLoginDTO dto) {
        return Result.ok(userService.githubLogin(dto.getCode()));
    }

    @Operation(summary = "QQ 登录（暂未开放）")
    @PostMapping("/qq")
    public Result<LoginVO> qq() {
        return Result.ok(userService.qqLogin(null));
    }

    @Operation(summary = "微信登录（暂未开放）")
    @PostMapping("/wechat")
    public Result<LoginVO> wechat() {
        return Result.ok(userService.wechatLogin(null));
    }

    @Operation(summary = "当前登录用户信息")
    @RequireLogin
    @PostMapping("/me")
    public Result<UserVO> me() {
        LoginUser user = UserContext.get();
        return Result.ok(userService.me(user.getUserId()));
    }
}
