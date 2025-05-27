package org.example.tiktok;

import jakarta.annotation.Resource;
import org.example.tiktok.dto.EmailCodeDTO;
import org.example.tiktok.service.LoginService;
import org.example.tiktok.utils.AliOSSUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.InputStream;

@SpringBootTest
class TikTokApplicationTests {

    @Resource
    LoginService userService;

    @Resource
    AliOSSUtil aliOSSUtil;

    @Test
    void testMail() {
        EmailCodeDTO emailCodeDTO = new EmailCodeDTO("661234","123456","1271892479@qq.com");
        userService.sendMail(emailCodeDTO);
    }

    //pass
    @Test
    public void testUploadFile() {
        try {
            // 本地图片路径
            String localFilePath = "E:\\png\\Logo.png";
            // 生成上传后的文件名（你可以自定义，也可以加时间戳防止重复）
            String objectName = "test/Logo.png";

            // 读取文件为 InputStream
            InputStream inputStream = new FileInputStream(localFilePath);

            // 调用上传方法
            String url = aliOSSUtil.uploadFile(objectName, inputStream);
            System.out.println("this is just a test");
            System.out.println("上传成功！访问地址：" + url);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
