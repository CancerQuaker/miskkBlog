package cn.miskk.blog.common.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 服务间批量查询用户请求（/internal/users/batch）。
 */
@Data
public class UserBatchDTO {

    @NotEmpty(message = "用户 ID 列表不能为空")
    private List<Long> ids;
}
