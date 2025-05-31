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
    /*const formData = new FormData();
formData.append('file', videoFile);
formData.append('type', 'video');
*/
    /*const formData = new FormData();
formData.append('file', avatarFile);
formData.append('type', 'avatar');*/
    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file,
                         @RequestParam("type") String type) throws Exception {
        // 判断上传类型，设定目录前缀
        String dirPrefix;
        if ("avatar".equalsIgnoreCase(type)) {
            dirPrefix = "avatar/";
        } else if ("video".equalsIgnoreCase(type)) {
            dirPrefix = "video/";
        } else {
            return Result.fail("Unsupported upload type: " + type);
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = dirPrefix + UUID.randomUUID().toString() + suffix;

        // 上传文件到阿里云 OSS
        String url = aliOSSUtil.uploadFile(filename, file.getInputStream());

        return Result.ok("Successfully uploaded " + type, url);
    }
}