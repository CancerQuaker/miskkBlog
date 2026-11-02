package cn.miskk.blog.web.handler;

import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.JwtUtil;
import cn.miskk.blog.common.auth.UserContext;
import cn.miskk.blog.common.dto.EmailCodeDTO;
import cn.miskk.blog.common.dto.GithubLoginDTO;
import cn.miskk.blog.common.dto.LoginDTO;
import cn.miskk.blog.common.dto.RegisterDTO;
import cn.miskk.blog.common.vo.LoginVO;
import cn.miskk.blog.common.vo.UserVO;
import cn.miskk.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 认证请求处理：UserService 只负责校验身份并回传用户信息，
 * 登录成功后在此统一签发会话 token（JWT 的签发在此处、解析在 AuthInterceptor，密钥仅本应用持有）。
 */
@Component
@RequiredArgsConstructor
public class AuthHandler {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public Result<String> sendCode(EmailCodeDTO dto) {
        return Result.ok(userService.sendEmailCode(dto.getEmail()));
    }

    public Result<UserVO> register(RegisterDTO dto) {
        return Result.ok(userService.register(dto));
    }

    public Result<LoginVO> login(LoginDTO dto) {
        return withToken(userService.login(dto));
    }

    public Result<LoginVO> github(GithubLoginDTO dto) {
        return withToken(userService.githubLogin(dto.getCode()));
    }

    public Result<LoginVO> qq() {
        return withToken(userService.qqLogin(null));
    }

    public Result<LoginVO> wechat() {
        return withToken(userService.wechatLogin(null));
    }

    public Result<UserVO> me() {
        return Result.ok(userService.me(UserContext.getUserId()));
    }

    /** 身份校验通过后，按用户 id/role 签发 JWT 填入响应 */
    private Result<LoginVO> withToken(LoginVO vo) {
        if (vo != null && vo.getUser() != null) {
            vo.setToken(jwtUtil.createToken(vo.getUser().getId(), vo.getUser().getRole()));
        }
        return Result.ok(vo);
    }
}
