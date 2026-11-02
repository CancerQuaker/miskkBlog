package cn.miskk.blog.web.handler;

import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.UserContext;
import cn.miskk.blog.common.dto.UpdatePasswordDTO;
import cn.miskk.blog.common.dto.UpdateProfileDTO;
import cn.miskk.blog.common.util.UploadUtil;
import cn.miskk.blog.common.vo.UserVO;
import cn.miskk.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户资料请求处理：当前用户取自 AuthInterceptor 填充的 UserContext。
 */
@Component
@RequiredArgsConstructor
public class UserHandler {

    private final UserService userService;

    @Value("${miskk.upload.dir:${user.home}/.miskk/uploads}")
    private String uploadDir;

    /** 头像等用户图片的访问前缀，与 WebConfig 中的静态资源映射对应 */
    @Value("${miskk.upload.user-url-prefix:/api/static/user}")
    private String urlPrefix;

    public Result<UserVO> me() {
        return Result.ok(userService.me(UserContext.getUserId()));
    }

    public Result<UserVO> updateProfile(UpdateProfileDTO dto) {
        return Result.ok(userService.updateProfile(UserContext.getUserId(), dto));
    }

    public Result<Void> updatePassword(UpdatePasswordDTO dto) {
        userService.updatePassword(UserContext.getUserId(), dto);
        return Result.ok();
    }

    public Result<UserVO> user(Long id) {
        return Result.ok(userService.getUserPublic(id));
    }

    /** 上传图片（头像），返回可直接访问的 URL */
    public Result<String> uploadImage(MultipartFile file) {
        String relative = UploadUtil.saveImage(file, uploadDir, "user");
        return Result.ok(urlPrefix + relative);
    }
}
