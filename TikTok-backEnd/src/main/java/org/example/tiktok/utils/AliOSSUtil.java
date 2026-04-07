package org.example.tiktok.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class AliOSSUtil {
    private static final String ENDPOINT = "https://oss-cn-shanghai.aliyuncs.com";

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    private static final String AVATAR_BUCKET = "save-avatar-tiktok";
    private static final String VIDEO_BUCKET = "tiktok-personal";

    public String uploadFile(String objectName, InputStream in, String type) {
        String bucketName = "video".equalsIgnoreCase(type) ? VIDEO_BUCKET : AVATAR_BUCKET;

        OSS ossClient = new OSSClientBuilder().build(ENDPOINT, accessKeyId, accessKeySecret);
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, in);
            ossClient.putObject(putObjectRequest);
            return "https://" + bucketName + "."
                    + ENDPOINT.substring(ENDPOINT.lastIndexOf("/") + 1)
                    + "/" + objectName;
        } catch (Exception e) {
            throw new RuntimeException("OSS upload failed: " + e.getMessage(), e);
        } finally {
            ossClient.shutdown();
        }
    }
}