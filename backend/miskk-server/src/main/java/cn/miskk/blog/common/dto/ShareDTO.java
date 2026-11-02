package cn.miskk.blog.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 分享上报请求。
 */
@Data
public class ShareDTO {

    /** 文章 ID（以路径变量为准，请求体中可省略） */
    private Long articleId;

    /** qq / wechat / copy */
    @NotBlank(message = "分享渠道不能为空")
    private String channel;
}
