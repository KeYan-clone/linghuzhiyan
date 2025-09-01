package org.linghu.resource.exception;

/**
 * 资源业务异常
 */
public class ResourceException extends RuntimeException {

    private final int code;

    public ResourceException(int code, String message) {
        super(message);
        this.code = code;
    }

    public ResourceException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    // 静态工厂方法
    public static ResourceException resourceNotFound() {
        return new ResourceException(404, "资源不存在");
    }

    public static ResourceException resourceNotFound(String resourceId) {
        return new ResourceException(404, "资源不存在: " + resourceId);
    }

    public static ResourceException fileNotFound() {
        return new ResourceException(404, "文件不存在");
    }

    public static ResourceException fileNotFound(String fileName) {
        return new ResourceException(404, "文件不存在: " + fileName);
    }

    public static ResourceException invalidFileFormat() {
        return new ResourceException(400, "不支持的文件格式");
    }

    public static ResourceException invalidFileFormat(String format) {
        return new ResourceException(400, "不支持的文件格式: " + format);
    }

    public static ResourceException fileEmpty() {
        return new ResourceException(400, "文件为空，上传失败");
    }


    public static ResourceException fileSizeExceeded() {
        return new ResourceException(413, "文件大小超出限制");
    }

    public static ResourceException fileSizeExceeded(long maxSize) {
        return new ResourceException(413, "文件大小超出限制，最大允许: " + maxSize + " bytes");
    }

    public static ResourceException uploadFailed() {
        return new ResourceException(500, "文件上传失败");
    }

    public static ResourceException uploadFailed(String reason) {
        return new ResourceException(500, "文件上传失败: " + reason);
    }

    public static ResourceException downloadFailed() {
        return new ResourceException(500, "文件下载失败");
    }

    public static ResourceException downloadFailed(String reason) {
        return new ResourceException(500, "文件下载失败: " + reason);
    }

    public static ResourceException deleteFailed() {
        return new ResourceException(500, "文件删除失败");
    }

    public static ResourceException deleteFailed(String reason) {
        return new ResourceException(500, "文件删除失败: " + reason);
    }

    public static ResourceException accessDenied() {
        return new ResourceException(403, "没有访问该资源的权限");
    }

    public static ResourceException accessDenied(String resourceId) {
        return new ResourceException(403, "没有访问资源的权限: " + resourceId);
    }

    public static ResourceException storageServiceUnavailable() {
        return new ResourceException(503, "存储服务不可用");
    }

    public static ResourceException invalidPath() {
        return new ResourceException(400, "非法的文件路径");
    }

    public static ResourceException invalidPath(String path) {
        return new ResourceException(400, "非法的文件路径: " + path);
    }

    public static ResourceException compressionFailed() {
        return new ResourceException(500, "文件压缩失败");
    }

    public static ResourceException decompressionFailed() {
        return new ResourceException(500, "文件解压失败");
    }

    public static ResourceException decompressionFailed(String reason) {
        return new ResourceException(500, "文件解压失败: " + reason);
    }

    public static ResourceException bucketNotFound() {
        return new ResourceException(404, "存储桶不存在");
    }

    public static ResourceException bucketNotFound(String bucketName) {
        return new ResourceException(404, "存储桶不存在: " + bucketName);
    }
}
