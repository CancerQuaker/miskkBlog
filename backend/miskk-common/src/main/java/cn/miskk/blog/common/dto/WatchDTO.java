package cn.miskk.blog.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 上报观看时长请求（前端每 20 秒心跳上报一次）。
 */
@Data
public class WatchDTO {

    @NotNull(message = "文章不能为空")
    private Long articleId;

    @NotNull(message = "时长不能为空")
    @Min(value = 1, message = "时长不合法")
    @Max(value = 60, message = "单次上报不能超过 60 秒")
    private Integer seconds;
}
