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
    // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
    //EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    // 填写Bucket名称，例如examplebucket。
    private static final String BUCKET_NAME = "save-avatar";

    public String uploadFile(String objectName, InputStream in) {
        OSS ossClient = new OSSClientBuilder().build(ENDPOINT,accessKeyId,accessKeySecret );
        String url = "";
        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, objectName, in);
        PutObjectResult result = ossClient.putObject(putObjectRequest);
        //url组成：https://bucket名称.区域节点/objectName
        url = "https://"+BUCKET_NAME+"."+ENDPOINT.substring(ENDPOINT.lastIndexOf("/")+1)+"/"+objectName;
        ossClient.shutdown();
        return url;
    }
}
