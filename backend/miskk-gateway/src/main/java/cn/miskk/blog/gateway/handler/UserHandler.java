package cn.miskk.blog.gateway.handler;

import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.UserContext;
import cn.miskk.blog.common.dto.IdDTO;
import cn.miskk.blog.common.dto.UpdatePasswordDTO;
import cn.miskk.blog.common.dto.UpdateProfileDTO;
import cn.miskk.blog.common.vo.UserVO;
import cn.miskk.blog.gateway.service.UserRemoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户资料请求处理：用户上下文以单个 X-User-Info 请求头（LoginUser JSON）下发。
 */
@Component
@RequiredArgsConstructor
public class UserHandler {

    private final UserRemoteService userRemoteService;

    public Result<UserVO> me() {
        return userRemoteService.myProfile(UserContext.toJson());
    }

    public Result<UserVO> updateProfile(UpdateProfileDTO dto) {
        return userRemoteService.updateProfile(UserContext.toJson(), dto);
    }

    public Result<Void> updatePassword(UpdatePasswordDTO dto) {
        return userRemoteService.updatePassword(UserContext.toJson(), dto);
    }

    public Result<UserVO> user(Long id) {
        IdDTO dto = new IdDTO();
        dto.setId(id);
        return userRemoteService.user(dto);
    }

    public Result<String> uploadImage(MultipartFile file) {
        return userRemoteService.uploadImage(UserContext.toJson(), file);
    }
}
