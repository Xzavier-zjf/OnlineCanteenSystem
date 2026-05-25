package com.canteen.user.controller;

import com.canteen.common.result.Result;
import com.canteen.common.utils.AuthContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {

    @Value("${file.upload.path:/uploads}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:http://localhost:8081}")
    private String urlPrefix;

    /**
     * 上传图片
     */
    @PostMapping("/image")
    public Result<Map<String, Object>> uploadImage(HttpServletRequest request,
                                                   @RequestParam("file") MultipartFile file) {
        try {
            AuthContext.from(request).requireRole("MERCHANT", "ADMIN");

            // 验证文件
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            String extension = getSafeImageExtension(file);
            if (extension == null || ImageIO.read(file.getInputStream()) == null) {
                return Result.error("只能上传图片文件");
            }

            // 验证文件大小 (2MB)
            if (file.getSize() > 2 * 1024 * 1024) {
                return Result.error("文件大小不能超过2MB");
            }

            // 创建上传目录
            String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fullUploadPath = uploadPath + "/images/" + dateDir;
            Path uploadDir = Paths.get(fullUploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // 构建返回URL
            String fileUrl = "/uploads/images/" + dateDir + "/" + filename;
            String fullUrl = urlPrefix + fileUrl;

            Map<String, Object> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("fullUrl", fullUrl);
            result.put("filename", filename);
            result.put("originalName", originalFilename);
            result.put("size", file.getSize());

            return Result.success(result);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Result<Map<String, Object>> uploadAvatar(HttpServletRequest request,
                                                    @RequestParam("file") MultipartFile file) {
        try {
            AuthContext.from(request).requireRole("USER", "MERCHANT", "ADMIN");

            // 验证文件
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            String extension = getSafeImageExtension(file);
            if (extension == null || ImageIO.read(file.getInputStream()) == null) {
                return Result.error("只能上传图片文件");
            }

            // 验证文件大小 (1MB)
            if (file.getSize() > 1024 * 1024) {
                return Result.error("头像文件大小不能超过1MB");
            }

            // 创建上传目录
            String fullUploadPath = uploadPath + "/avatars";
            Path uploadDir = Paths.get(fullUploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String filename = "avatar_" + System.currentTimeMillis() + extension;

            // 保存文件
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // 构建返回URL
            String avatarUrl = "/uploads/avatars/" + filename;
            String fullUrl = urlPrefix + avatarUrl;

            Map<String, Object> result = new HashMap<>();
            result.put("avatarUrl", avatarUrl);
            result.put("fullUrl", fullUrl);
            result.put("filename", filename);
            result.put("originalName", originalFilename);
            result.put("size", file.getSize());

            return Result.success(result);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/file")
    public Result<String> deleteFile(HttpServletRequest request, @RequestParam("url") String fileUrl) {
        try {
            AuthContext.from(request).requireRole("MERCHANT", "ADMIN");

            if (!fileUrl.startsWith("/uploads/")) {
                return Result.error("无效的文件路径");
            }

            Path root = Paths.get(uploadPath).toAbsolutePath().normalize();
            Path path = root.resolve(fileUrl.substring("/uploads/".length())).normalize();
            if (!path.startsWith(root)) {
                return Result.error("无效的文件路径");
            }

            if (Files.exists(path)) {
                Files.delete(path);
                return Result.success("文件删除成功");
            }
            return Result.error("文件不存在");
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }

    private String getSafeImageExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return null;
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        switch (extension) {
            case ".jpg":
            case ".jpeg":
            case ".png":
            case ".gif":
            case ".webp":
                return extension;
            default:
                return null;
        }
    }
}
