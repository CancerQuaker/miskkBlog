package cn.miskk.blog.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分享上报请求。
 */
@Data
public class ShareDTO {

    @NotNull(message = "文章不能为空")
    private Long articleId;

    /** qq / wechat / copy */
    @NotBlank(message = "分享渠道不能为空")
    private String channel;
}
