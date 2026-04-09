package org.example.tiktok.controller;

import jakarta.annotation.Resource;
import org.example.tiktok.entity.Result;
import org.example.tiktok.utils.AliOSSUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
public class FileUploadController {
    @Resource
    AliOSSUtil aliOSSUtil;

    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file,
                         @RequestParam("type") String type,
                         @RequestParam(value = "category", defaultValue = "general") String category) {
        // Validate type
        String dirPrefix;
        if ("avatar".equalsIgnoreCase(type)) {
            dirPrefix = "avatar/";
        } else if ("video".equalsIgnoreCase(type)) {
            dirPrefix = "video/" + category + "/";
        } else {
            return Result.fail("Unsupported upload type: " + type);
        }

        // Validate file
        if (file == null || file.isEmpty()) {
            return Result.fail("file is empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return Result.fail("invalid file name");
        }

        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = dirPrefix + UUID.randomUUID() + suffix;

        try {
            String url = aliOSSUtil.uploadFile(filename, file.getInputStream(), type);
            System.out.println("filename:"+filename);
            return Result.ok("Successfully uploaded " + type, url);
        } catch (Exception e) {
            return Result.fail("Upload failed: " + e.getMessage());
        }
    }
}