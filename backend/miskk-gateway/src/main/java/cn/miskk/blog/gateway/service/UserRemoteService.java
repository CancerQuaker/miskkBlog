package cn.miskk.blog.gateway.service;

import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.constant.SecurityConstants;
import cn.miskk.blog.common.dto.EmailCodeDTO;
import cn.miskk.blog.common.dto.GithubLoginDTO;
import cn.miskk.blog.common.dto.IdDTO;
import cn.miskk.blog.common.dto.LoginDTO;
import cn.miskk.blog.common.dto.RegisterDTO;
import cn.miskk.blog.common.dto.UpdatePasswordDTO;
import cn.miskk.blog.common.dto.UpdateProfileDTO;
import cn.miskk.blog.common.vo.LoginVO;
import cn.miskk.blog.common.vo.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务远程接口（Feign，统一 POST、不使用路径参数：资源 ID 一律放请求体 DTO）。
 * <p>
 * 请求/响应对象统一取自 common 的 DTO/VO；
 * 用户上下文由网关解析 JWT 后，以 {@code @RequestHeader} 参数形式显式下发
 * （userId → X-User-Id，role → X-User-Role），参数值为 null 时 Feign 自动省略该请求头，
 * 下游收到即视为匿名请求。
 */
@FeignClient(name = "miskk-user", contextId = "gatewayUserClient")
public interface UserRemoteService {

    @PostMapping("/auth/code")
    Result<String> sendCode(@RequestBody EmailCodeDTO dto);

    @PostMapping("/auth/register")
    Result<UserVO> register(@RequestBody RegisterDTO dto);

    /** 账号密码登录（邮箱或用户名），成功返回 JWT + 用户信息 */
    @PostMapping("/auth/login")
    Result<LoginVO> login(@RequestBody LoginDTO dto);

    @PostMapping("/auth/github")
    Result<LoginVO> github(@RequestBody GithubLoginDTO dto);

    @PostMapping("/auth/qq")
    Result<LoginVO> qq();

    @PostMapping("/auth/wechat")
    Result<LoginVO> wechat();

    /** 当前登录用户信息（/auth/me，需登录） */
    @PostMapping("/auth/me")
    Result<UserVO> me(@RequestHeader(SecurityConstants.HEADER_USER_ID) Long userId,
                      @RequestHeader(SecurityConstants.HEADER_USER_ROLE) Integer role);

    /** 我的资料（/user/me，需登录） */
    @PostMapping("/user/me")
    Result<UserVO> myProfile(@RequestHeader(SecurityConstants.HEADER_USER_ID) Long userId,
                             @RequestHeader(SecurityConstants.HEADER_USER_ROLE) Integer role);

    @PostMapping("/user/me/update")
    Result<UserVO> updateProfile(@RequestHeader(SecurityConstants.HEADER_USER_ID) Long userId,
                                 @RequestHeader(SecurityConstants.HEADER_USER_ROLE) Integer role,
                                 @RequestBody UpdateProfileDTO dto);

    @PostMapping("/user/me/password")
    Result<Void> updatePassword(@RequestHeader(SecurityConstants.HEADER_USER_ID) Long userId,
                                @RequestHeader(SecurityConstants.HEADER_USER_ROLE) Integer role,
                                @RequestBody UpdatePasswordDTO dto);

    /** 用户公开主页信息 */
    @PostMapping("/users")
    Result<UserVO> user(@RequestBody IdDTO dto);

    /** 头像上传（需登录），返回可访问的图片 URL */
    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<String> uploadImage(@RequestHeader(SecurityConstants.HEADER_USER_ID) Long userId,
                               @RequestHeader(SecurityConstants.HEADER_USER_ROLE) Integer role,
                               @RequestPart("file") MultipartFile file);
}
