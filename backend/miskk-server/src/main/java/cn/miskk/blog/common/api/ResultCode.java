package cn.miskk.blog.common.api;

import lombok.Getter;

/**
 * 统一响应码。
 */
@Getter
public enum ResultCode {

    SUCCESS(0, "成功"),
    FAIL(500, "服务异常，请稍后再试"),
    PARAM_ERROR(400, "请求参数错误"),
    UNAUTHORIZED(401, "请先登录"),
    FORBIDDEN(403, "没有权限执行该操作"),
    NOT_FOUND(404, "资源不存在");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
