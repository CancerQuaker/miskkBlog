package cn.miskk.blog.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通用 ID 请求：服务间 POST 接口不使用路径参数，仅传 ID 的操作
 * （查详情/浏览/点赞/取消点赞/删除等）统一使用本对象。
 */
@Data
public class IdDTO {

    /** 目标资源 ID */
    @NotNull(message = "ID 不能为空")
    private Long id;
}
