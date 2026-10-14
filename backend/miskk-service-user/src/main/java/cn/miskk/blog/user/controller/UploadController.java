package cn.miskk.blog.user.controller;

import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.auth.RequireLogin;
import cn.miskk.blog.common.util.UploadUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片上传（头像等）。
 */
@Tag(name = "上传")
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${miskk.upload.dir:${user.home}/.miskk/uploads}")
    private String uploadDir;

    @Value("${miskk.upload.url-prefix:/api/static/user}")
    private String urlPrefix;

    @Operation(summary = "上传图片（头像）")
    @RequireLogin
    @PostMapping("/image")
    public Result<String> image(@RequestPart("file") MultipartFile file) {
        String relative = UploadUtil.saveImage(file, uploadDir, "user");
        return Result.ok(urlPrefix + relative);
    }
}
