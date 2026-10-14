package cn.miskk.blog.gateway.controller;

import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.dto.UpdatePasswordDTO;
import cn.miskk.blog.common.dto.UpdateProfileDTO;
import cn.miskk.blog.common.vo.UserVO;
import cn.miskk.blog.gateway.handler.UserHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户资料入口（对前端保持原请求方式：GET 查询、PUT 修改），请求/响应对象取自 common 共享 DTO/VO。
 */
@Tag(name = "网关-用户资料")
@RestController
@RequiredArgsConstructor
public class GatewayUserController {

    private final UserHandler userHandler;

    @Operation(summary = "我的资料")
    @GetMapping("/api/user/user/me")
    public Result<UserVO> me() {
        return userHandler.me();
    }

    @Operation(summary = "修改我的资料")
    @PutMapping("/api/user/user/me")
    public Result<UserVO> updateMe(@RequestBody UpdateProfileDTO dto) {
        return userHandler.updateProfile(dto);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/api/user/user/me/password")
    public Result<Void> updatePassword(@RequestBody UpdatePasswordDTO dto) {
        return userHandler.updatePassword(dto);
    }

    @Operation(summary = "用户公开主页信息")
    @GetMapping("/api/user/users/{id}")
    public Result<UserVO> user(@PathVariable Long id) {
        return userHandler.user(id);
    }

    @Operation(summary = "头像上传")
    @PostMapping("/api/user/upload/image")
    public Result<String> uploadImage(@RequestPart("file") MultipartFile file) {
        return userHandler.uploadImage(file);
    }
}
