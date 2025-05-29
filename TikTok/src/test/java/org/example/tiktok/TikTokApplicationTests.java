package org.example.tiktok;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import org.example.tiktok.dto.EmailCodeDTO;
import org.example.tiktok.mapper.VideoMapper;
import org.example.tiktok.service.IndexService;
import org.example.tiktok.service.LoginService;
import org.example.tiktok.utils.AliOSSUtil;
import org.example.tiktok.utils.CacheClient;
import org.example.tiktok.utils.HotRank;
import org.example.tiktok.utils.SnowflakeIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.InputStream;

@SpringBootTest
class TikTokApplicationTests {

    @Resource
    LoginService userService;

    @Resource
    IndexService indexService;

    @Resource
    AliOSSUtil aliOSSUtil;

    @Resource
    SnowflakeIdWorker snowflakeIdWorker;

    @Resource
    HotRank hotRank;

    @Resource
    VideoMapper videoMapper;

    @Resource
    CacheClient cacheClient;

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
            String objectName = "Thisisatest/Logo.png";

            // 读取文件为 InputStream
            InputStream inputStream = new FileInputStream(localFilePath);

            // 调用上传方法
            String url = aliOSSUtil.uploadFile(objectName, inputStream);
            System.out.println("this test for git is work");
            System.out.println("上传成功！访问地址：" + url);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testSnow() {
        System.out.println(snowflakeIdWorker.nextId());
        System.out.println(snowflakeIdWorker.nextId());
    }

    @Test
    public void testHotRank() throws JsonProcessingException {
        hotRank.calculateDailyHotRank();
        System.out.println(indexService.getHotRank());
    }




}
