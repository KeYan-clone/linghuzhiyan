package org.linghu.resource.exception;

import org.linghu.resource.dto.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        logger.error("参数验证异常", e);
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((s1, s2) -> s1 + "; " + s2)
                .orElse("参数验证失败");
        return Result.error(400, message);
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingParameterException(MissingServletRequestParameterException e) {
        logger.error("缺少请求参数", e);
        return Result.error(400, "缺少必需参数: " + e.getParameterName());
    }

    /**
     * 处理缺少文件参数异常
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingFileException(MissingServletRequestPartException e) {
        logger.error("缺少文件参数", e);
        return Result.error(400, "缺少必需文件: " + e.getRequestPartName());
    }

    /**
     * 处理访问拒绝异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        logger.warn("访问被拒绝", e);
        return Result.error(403, "访问被拒绝");
    }

    /**
     * 处理HTTP消息不可读异常（如JSON格式错误、请求体为空等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        logger.error("HTTP消息不可读异常", e);
        String message = "请求体格式错误或为空";
        if (e.getCause() != null) {
            String causeMessage = e.getCause().getMessage();
            if (causeMessage != null && causeMessage.contains("JSON")) {
                message = "JSON格式错误";
            }
        }
        return Result.error(400, message);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.error("非法参数异常", e);
        String message = e.getMessage();

        // 针对资源ID相关的异常提供更具体的错误信息
        if (message != null && message.toLowerCase().contains("invalid resource id")) {
            return Result.error(400, "无效的资源ID格式");
        }

        return Result.error(400, message != null ? message : "参数不合法");
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleNullPointerException(NullPointerException e) {
        logger.error("空指针异常", e);
        return Result.error(500, "系统内部错误");
    }

    /**
     * 处理通用运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        logger.error("运行时异常", e);
        return Result.error(500, e.getMessage());
    }

    /**
     * 处理资源未找到异常
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoResourceFoundException(NoResourceFoundException e) {
        logger.warn("资源未找到异常", e);
        return Result.error(404, "请求的资源不存在");
    }

    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleGenericException(Exception e) {
        logger.error("未知异常", e);
        return Result.error(500, "系统内部错误");
    }
}
