package cn.miskk.blog.gateway.exception;

import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.api.ResultCode;
import cn.miskk.blog.common.exception.BizException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 网关统一异常处理：始终返回 HTTP 200 + 统一响应结构，由前端按 code 提示。
 */
@Slf4j
@RestControllerAdvice
public class GatewayExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e) {
        log.debug("网关业务校验未通过: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class})
    public Result<Void> handleParam(Exception e) {
        log.debug("网关参数异常: {}", e.getMessage());
        return Result.fail(400, "请求参数错误");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleUploadSize(MaxUploadSizeExceededException e) {
        return Result.fail("图片大小不能超过 5MB");
    }

    @ExceptionHandler(FeignException.class)
    public Result<Void> handleFeign(FeignException e) {
        log.warn("下游服务调用失败: status={}, message={}", e.status(), e.getMessage());
        return Result.fail(503, "服务暂时不可用，请稍后再试");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        log.error("网关未知异常", e);
        return Result.fail(ResultCode.FAIL);
    }
}
