package cn.miskk.blog.gateway.handler;

import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.JwtUtil;
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
 * 用户服务只负责校验身份，登录成功后由网关统一签发会话 token
 * （JWT 的签发与解析都收敛在网关，JWT_SECRET 仅网关持有）。
 */
@Component
@RequiredArgsConstructor
public class AuthHandler {

    private final UserRemoteService userRemoteService;
    private final JwtUtil jwtUtil;

    public Result<String> sendCode(EmailCodeDTO dto) {
        return userRemoteService.sendCode(dto);
    }

    public Result<UserVO> register(RegisterDTO dto) {
        return userRemoteService.register(dto);
    }

    public Result<LoginVO> login(LoginDTO dto) {
        return withToken(userRemoteService.login(dto));
    }

    public Result<LoginVO> github(GithubLoginDTO dto) {
        return withToken(userRemoteService.github(dto));
    }

    public Result<LoginVO> qq() {
        return withToken(userRemoteService.qq());
    }

    public Result<LoginVO> wechat() {
        return withToken(userRemoteService.wechat());
    }

    public Result<UserVO> me() {
        return userRemoteService.me(UserContext.toJson());
    }

    /** user 服务校验通过后，网关按用户 id/role 签发 JWT 填入响应 */
    private Result<LoginVO> withToken(Result<LoginVO> result) {
        LoginVO vo = result.getData();
        if (result.getCode() == 0 && vo != null && vo.getUser() != null) {
            vo.setToken(jwtUtil.createToken(vo.getUser().getId(), vo.getUser().getRole()));
        }
        return result;
    }
}
