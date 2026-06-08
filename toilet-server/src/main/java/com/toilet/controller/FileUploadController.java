package com.toilet.controller;

import com.toilet.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"));

    private static final Set<String> ALLOWED_MAGIC = new HashSet<>(Arrays.asList(
            "ffd8ffe0", "ffd8ffe1", "ffd8ffe2", // JPEG
            "89504e47", // PNG
            "47494638", // GIF
            "52494646"  // WEBP (RIFF)
    ));

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir, "images"));
        } catch (IOException e) {
            log.warn("上传目录创建失败", e);
        }
    }

    @PostMapping("/image")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLEANER', 'REPAIR')")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.badRequest("请选择文件");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.badRequest("文件大小不能超过10MB");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            return Result.badRequest("文件格式不正确");
        }

        String ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return Result.badRequest("仅支持 JPG/PNG/GIF/WebP 格式的图片");
        }

        // 魔数校验（文件头签名）
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            if (is.read(header) < 4) {
                return Result.badRequest("文件内容无效");
            }
            StringBuilder magic = new StringBuilder();
            for (byte b : header) {
                magic.append(String.format("%02x", b));
            }
            if (!ALLOWED_MAGIC.contains(magic.toString())) {
                return Result.badRequest("文件内容与图片格式不匹配，请检查文件是否损坏");
            }
        } catch (IOException e) {
            log.error("读取文件头失败", e);
            return Result.error("文件读取失败");
        }

        try {
            String fileName = UUID.randomUUID() + ext;
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path dirPath = Paths.get(uploadDir, "images", datePath);
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(fileName);
            file.transferTo(filePath.toFile());
            String url = "/uploads/images/" + datePath + "/" + fileName;
            log.info("文件上传成功: {} ({})", url, file.getSize());
            return Result.success(url);
        } catch (IOException e) {
            log.error("文件保存失败", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}
