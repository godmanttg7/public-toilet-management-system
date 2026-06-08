package com.toilet.exception;

import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }

    /**
     * 业务错误：参数错误
     */
    public static BusinessException badRequest(String message) {
        return new BusinessException(400, message);
    }

    /**
     * 业务错误：未授权
     */
    public static BusinessException unauthorized(String message) {
        return new BusinessException(401, message);
    }

    /**
     * 业务错误：禁止访问
     */
    public static BusinessException forbidden(String message) {
        return new BusinessException(403, message);
    }

    /**
     * 业务错误：资源不存在
     */
    public static BusinessException notFound(String message) {
        return new BusinessException(404, message);
    }

    /**
     * 业务错误：内部服务器错误
     */
    public static BusinessException internalError(String message) {
        return new BusinessException(500, message);
    }

    /**
     * 业务错误：密码错误
     */
    public static BusinessException passwordError() {
        return new BusinessException(1001, "用户名或密码错误");
    }

    /**
     * 业务错误：账号被禁用
     */
    public static BusinessException accountDisabled() {
        return new BusinessException(1002, "账号已被禁用，请联系管理员");
    }

    /**
     * 业务错误：权限不足
     */
    public static BusinessException permissionDenied(String resource) {
        return new BusinessException(1003, String.format("无权限访问资源：%s", resource));
    }

    /**
     * 业务错误：数据已存在
     */
    public static BusinessException dataExists(String resource) {
        return new BusinessException(1004, String.format("%s已存在，请勿重复提交", resource));
    }

    /**
     * 业务错误：数据不存在
     */
    public static BusinessException dataNotFound(String resource) {
        return new BusinessException(1005, String.format("%s不存在", resource));
    }

    /**
     * 业务错误：操作失败
     */
    public static BusinessException operationFailed(String message) {
        return new BusinessException(1006, message);
    }
}
