package cn.miskk.blog.gateway.handler;

import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.LoginUser;
import cn.miskk.blog.common.auth.UserContext;
import cn.miskk.blog.common.dto.EmailCodeDTO;
import cn.miskk.blog.common.dto.GithubLoginDTO;
import cn.miskk.blog.common.dto.LoginDTO;
import cn.miskk.blog.common.dto.RegisterDTO;
import cn.miskk.blog.common.vo.LoginVO;
import cn.miskk.blog.common.vo.UserVO;
import cn.miskk.blog.gateway.service.UserRemoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 认证请求处理：请求对象直接透传给用户服务；
 * 需要登录态的接口从 UserContext 取出用户上下文，以参数形式显式下发。
 */
@Component
@RequiredArgsConstructor
public class AuthHandler {

    private final UserRemoteService userRemoteService;

    public Result<String> sendCode(EmailCodeDTO dto) {
        return userRemoteService.sendCode(dto);
    }

    public Result<UserVO> register(RegisterDTO dto) {
        return userRemoteService.register(dto);
    }

    public Result<LoginVO> login(LoginDTO dto) {
        return userRemoteService.login(dto);
    }

    public Result<LoginVO> github(GithubLoginDTO dto) {
        return userRemoteService.github(dto);
    }

    public Result<LoginVO> qq() {
        return userRemoteService.qq();
    }

    public Result<LoginVO> wechat() {
        return userRemoteService.wechat();
    }

    public Result<UserVO> me() {
        LoginUser user = UserContext.get();
        return userRemoteService.me(user.getUserId(), user.getRole());
    }
}
