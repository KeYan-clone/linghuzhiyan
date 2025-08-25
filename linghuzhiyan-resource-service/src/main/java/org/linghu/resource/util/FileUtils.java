package org.linghu.resource.util;

import org.springframework.util.StringUtils;

/**
 * 文件工具类
 */
public class FileUtils {

    /**
     * 根据文件名和MIME类型自动检测资源类型
     */
    public static String detectResourceType(String fileName, String mimeType) {
        if (fileName == null) {
            return "OTHER";
        }

        String lowerFileName = fileName.toLowerCase();

        // 根据文件扩展名判断资源类型
        if (lowerFileName.endsWith(".pdf")) {
            return "DOCUMENT";
        } else if (lowerFileName.endsWith(".doc") || lowerFileName.endsWith(".docx") ||
                lowerFileName.endsWith(".txt") || lowerFileName.endsWith(".md") ||
                lowerFileName.endsWith(".rtf") || lowerFileName.endsWith(".odt")) {
            return "DOCUMENT";
        } else if (lowerFileName.endsWith(".ppt") || lowerFileName.endsWith(".pptx") ||
                lowerFileName.endsWith(".odp")) {
            return "DOCUMENT";
        } else if (lowerFileName.endsWith(".xls") || lowerFileName.endsWith(".xlsx") ||
                lowerFileName.endsWith(".ods") || lowerFileName.endsWith(".csv")) {
            return "DOCUMENT";
        } else if (lowerFileName.endsWith(".mp4") || lowerFileName.endsWith(".avi") ||
                lowerFileName.endsWith(".mov") || lowerFileName.endsWith(".wmv") ||
                lowerFileName.endsWith(".flv") || lowerFileName.endsWith(".mkv") ||
                lowerFileName.endsWith(".webm") || lowerFileName.endsWith(".m4v")) {
            return "VIDEO";
        } else if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg") ||
                lowerFileName.endsWith(".png") || lowerFileName.endsWith(".gif") ||
                lowerFileName.endsWith(".bmp") || lowerFileName.endsWith(".webp") ||
                lowerFileName.endsWith(".svg") || lowerFileName.endsWith(".ico")) {
            return "IMAGE";
        } else if (lowerFileName.endsWith(".mp3") || lowerFileName.endsWith(".wav") ||
                lowerFileName.endsWith(".flac") || lowerFileName.endsWith(".aac") ||
                lowerFileName.endsWith(".ogg") || lowerFileName.endsWith(".wma")) {
            return "AUDIO";
        } else if (lowerFileName.endsWith(".zip") || lowerFileName.endsWith(".rar") ||
                lowerFileName.endsWith(".7z") || lowerFileName.endsWith(".tar") ||
                lowerFileName.endsWith(".gz") || lowerFileName.endsWith(".bz2")) {
            return "ARCHIVE";
        } else if (lowerFileName.endsWith(".java") || lowerFileName.endsWith(".cpp") ||
                lowerFileName.endsWith(".c") || lowerFileName.endsWith(".py") ||
                lowerFileName.endsWith(".js") || lowerFileName.endsWith(".html") ||
                lowerFileName.endsWith(".css") || lowerFileName.endsWith(".sql") ||
                lowerFileName.endsWith(".json") || lowerFileName.endsWith(".xml") ||
                lowerFileName.endsWith(".yaml") || lowerFileName.endsWith(".yml")) {
            return "CODE";
        }

        // 根据MIME类型判断
        if (mimeType != null) {
            String lowerMimeType = mimeType.toLowerCase();
            if (lowerMimeType.startsWith("image/")) {
                return "IMAGE";
            } else if (lowerMimeType.startsWith("video/")) {
                return "VIDEO";
            } else if (lowerMimeType.startsWith("audio/")) {
                return "AUDIO";
            } else if (lowerMimeType.startsWith("text/") || 
                      lowerMimeType.contains("document") ||
                      lowerMimeType.contains("pdf") ||
                      lowerMimeType.contains("word") ||
                      lowerMimeType.contains("excel") ||
                      lowerMimeType.contains("powerpoint")) {
                return "DOCUMENT";
            }
        }

        return "OTHER";
    }

    /**
     * 根据文件名获取MIME类型
     */
    public static String getMimeTypeFromFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "application/octet-stream";
        }

        String extension = "";
        if (fileName.contains(".")) {
            extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        }

        switch (extension) {
            case ".pdf":
                return "application/pdf";
            case ".doc":
                return "application/msword";
            case ".docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case ".xls":
                return "application/vnd.ms-excel";
            case ".xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case ".ppt":
                return "application/vnd.ms-powerpoint";
            case ".pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case ".txt":
                return "text/plain";
            case ".html":
                return "text/html";
            case ".css":
                return "text/css";
            case ".js":
                return "application/javascript";
            case ".json":
                return "application/json";
            case ".xml":
                return "application/xml";
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            case ".bmp":
                return "image/bmp";
            case ".webp":
                return "image/webp";
            case ".svg":
                return "image/svg+xml";
            case ".mp4":
                return "video/mp4";
            case ".avi":
                return "video/x-msvideo";
            case ".mov":
                return "video/quicktime";
            case ".wmv":
                return "video/x-ms-wmv";
            case ".flv":
                return "video/x-flv";
            case ".mkv":
                return "video/x-matroska";
            case ".webm":
                return "video/webm";
            case ".mp3":
                return "audio/mpeg";
            case ".wav":
                return "audio/wav";
            case ".flac":
                return "audio/flac";
            case ".aac":
                return "audio/aac";
            case ".ogg":
                return "audio/ogg";
            case ".zip":
                return "application/zip";
            case ".rar":
                return "application/x-rar-compressed";
            case ".7z":
                return "application/x-7z-compressed";
            case ".tar":
                return "application/x-tar";
            case ".gz":
                return "application/gzip";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * 格式化文件大小
     */
    public static String formatFileSize(Long fileSize) {
        if (fileSize == null || fileSize <= 0) {
            return "0 B";
        }

        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = fileSize.doubleValue();

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", size, units[unitIndex]);
    }

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 验证文件名是否安全
     */
    public static boolean isValidFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return false;
        }

        // 检查是否包含危险字符
        String[] dangerousChars = {"..", "/", "\\", ":", "*", "?", "\"", "<", ">", "|"};
        for (String dangerousChar : dangerousChars) {
            if (fileName.contains(dangerousChar)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 清理文件名，移除危险字符
     */
    public static String sanitizeFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "unknown";
        }

        // 替换危险字符
        return fileName.replaceAll("[.]{2,}", ".")
                      .replaceAll("[/\\\\:*?\"<>|]", "_")
                      .trim();
    }
}
