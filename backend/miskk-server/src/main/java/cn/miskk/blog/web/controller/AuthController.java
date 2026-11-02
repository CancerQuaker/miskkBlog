package cn.miskk.blog.web.controller;

import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.RequireLogin;
import cn.miskk.blog.common.dto.EmailCodeDTO;
import cn.miskk.blog.common.dto.GithubLoginDTO;
import cn.miskk.blog.common.dto.LoginDTO;
import cn.miskk.blog.common.dto.RegisterDTO;
import cn.miskk.blog.common.vo.LoginVO;
import cn.miskk.blog.common.vo.UserVO;
import cn.miskk.blog.web.handler.AuthHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证入口（注册、登录（邮箱/GitHub/QQ/微信）、当前用户），请求/响应对象取自 common 共享 DTO/VO。
 */
@Tag(name = "认证")
@RestController
@RequestMapping("/api/user/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthHandler authHandler;

    @Operation(summary = "发送邮箱验证码")
    @PostMapping("/code")
    public Result<String> sendCode(@Valid @RequestBody EmailCodeDTO dto) {
        return authHandler.sendCode(dto);
    }

    @Operation(summary = "邮箱验证码注册")
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO dto) {
        return authHandler.register(dto);
    }

    @Operation(summary = "账号密码登录（邮箱或用户名）")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return authHandler.login(dto);
    }

    @Operation(summary = "GitHub OAuth 登录")
    @PostMapping("/github")
    public Result<LoginVO> github(@Valid @RequestBody GithubLoginDTO dto) {
        return authHandler.github(dto);
    }

    @Operation(summary = "QQ 登录（暂未开放）")
    @PostMapping("/qq")
    public Result<LoginVO> qq() {
        return authHandler.qq();
    }

    @Operation(summary = "微信登录（暂未开放）")
    @PostMapping("/wechat")
    public Result<LoginVO> wechat() {
        return authHandler.wechat();
    }

    @Operation(summary = "当前登录用户信息")
    @RequireLogin
    @GetMapping("/me")
    public Result<UserVO> me() {
        return authHandler.me();
    }
}
