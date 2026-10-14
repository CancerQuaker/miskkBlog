package cn.miskk.blog.common.exception;

import cn.miskk.blog.common.api.ResultCode;
import lombok.Getter;

/**
 * 业务异常，由全局异常处理器统一转换为 Result 响应。
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        super(message);
        this.code = ResultCode.FAIL.getCode();
    }

    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
}
