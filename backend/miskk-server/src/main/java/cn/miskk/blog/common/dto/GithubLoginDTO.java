package cn.miskk.blog.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * GitHub OAuth 登录请求（授权码模式，回调 code）。
 */
@Data
public class GithubLoginDTO {

    @NotBlank(message = "GitHub 授权码不能为空")
    private String code;
}
