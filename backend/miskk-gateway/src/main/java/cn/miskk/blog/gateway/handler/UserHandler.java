package cn.miskk.blog.gateway.handler;

import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.LoginUser;
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
 * 用户资料请求处理：从 UserContext 取出用户上下文，以参数形式显式下发。
 */
@Component
@RequiredArgsConstructor
public class UserHandler {

    private final UserRemoteService userRemoteService;

    public Result<UserVO> me() {
        LoginUser user = UserContext.get();
        return userRemoteService.myProfile(user.getUserId(), user.getRole());
    }

    public Result<UserVO> updateProfile(UpdateProfileDTO dto) {
        LoginUser user = UserContext.get();
        return userRemoteService.updateProfile(user.getUserId(), user.getRole(), dto);
    }

    public Result<Void> updatePassword(UpdatePasswordDTO dto) {
        LoginUser user = UserContext.get();
        return userRemoteService.updatePassword(user.getUserId(), user.getRole(), dto);
    }

    public Result<UserVO> user(Long id) {
        IdDTO dto = new IdDTO();
        dto.setId(id);
        return userRemoteService.user(dto);
    }

    public Result<String> uploadImage(MultipartFile file) {
        LoginUser user = UserContext.get();
        return userRemoteService.uploadImage(user.getUserId(), user.getRole(), file);
    }
}
