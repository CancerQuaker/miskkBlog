package cn.miskk.blog.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 友链审核请求。
 */
@Data
public class FriendReviewDTO {

    /** 待审核的申请 ID */
    @NotNull(message = "申请 ID 不能为空")
    private Long applyId;

    /** true 通过 / false 驳回 */
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;

    private String note;
}
