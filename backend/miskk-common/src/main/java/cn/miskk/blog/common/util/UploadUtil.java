package cn.miskk.blog.common.util;

import cn.hutool.core.util.IdUtil;
import cn.miskk.blog.common.exception.BizException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

/**
 * 图片上传工具：校验扩展名与大小，落盘到 {uploadDir}/{subDir}/{yyyyMMdd}/{uuid}.{ext}。
 * 返回相对访问路径 "/uploads/{subDir}/{yyyyMMdd}/{uuid}.{ext}"。
 */
public final class UploadUtil {

    private UploadUtil() {
    }

    private static final List<String> ALLOWED_EXT = List.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_SIZE = 5 * 1024 * 1024;

    public static String saveImage(MultipartFile file, String uploadDir, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择要上传的图片");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BizException("图片大小不能超过 5MB");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BizException("仅支持 jpg / jpeg / png / gif / webp 格式");
        }

        String dateDir = cn.hutool.core.date.DateUtil.format(new java.util.Date(), "yyyyMM");
        String relative = "/uploads/" + subDir + "/" + dateDir + "/" + IdUtil.fastSimpleUUID() + "." + ext;
        // 去掉开头的 /uploads 之外的路径分隔符风险
        String relativeName = relative.substring("/uploads/".length());
        Path target = Paths.get(uploadDir, relativeName.split("/"));

        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target.toAbsolutePath());
        } catch (IOException e) {
            throw new BizException("图片保存失败，请稍后再试");
        }
        return relative;
    }
}
